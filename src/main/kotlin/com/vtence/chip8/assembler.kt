package com.vtence.chip8

import java.nio.ByteBuffer


object Assembler {

    private const val base = 0x200

    private val symbolTable = SymbolTable(base)

    fun assemble(program: Program): Assembly {
        val assembly = Assembly.allocate(0x1000, base = base)
        assemble(program, into = assembly)
        resolveSymbols(assembly)
        return assembly
    }

    private fun assemble(program: Program, into: Assembly) {
        program.forEach { statement ->
            when (statement) {
                is AssemblyCode -> assemble(statement, into)
                is LabelDefinition -> defineLabel(statement, into)
                is Comment -> {}
                BlankLine -> {}
            }
        }
    }

    private fun resolveSymbols(assembly: Assembly) {
        symbolTable.resolve { (offset: Int, address: Int) ->
            assembly[offset] = Word(assembly[offset].high or address.msb.low, address.lsb)
        }
    }

    private fun assemble(code: AssemblyCode, into: Assembly) {
        val opCode = InstructionsTable
            .list(code.mnemonic, arity = code.operands.size)
            .map { runCatching { it.assemble(Arguments(code.operands, into.position, symbolTable)) } }
            .flatMap { it.asSequence() }
            .firstOrNull()
            ?: throw SyntaxException(code.toString())

        into.write(opCode.bytes())
    }

    private fun defineLabel(statement: LabelDefinition, into: Assembly) {
        symbolTable[statement.label] = into.position
    }
}

fun assemble(sourceCode: String) = Assembler.assemble(Program.fromSource(sourceCode))


class SymbolTable(private val defaultAddress: Int) {
    private val resolved = mutableMapOf<String, Int>()
    private val unresolved = mutableMapOf<Int, String>()

    operator fun set(label: String, address: Int) {
        if (label in resolved) throw IllegalArgumentException("label `$label` already defined")
        resolved[label] = address
    }

    operator fun contains(symbol: String) = symbol in resolved

    operator fun get(symbol: String): Int =
        resolved[symbol] ?: throw IllegalArgumentException("symbol `$symbol` not found")

    fun unresolved(atAddress: Int, symbol: String): String {
        unresolved[atAddress] = symbol
        return defaultAddress.toString(16)
    }

    fun resolve(action: (Pair<Int, Int>) -> Unit) {
        unresolved
            .map { (address, symbol) -> address to get(symbol) }
            .forEach {
                action(it)
                markResolved(it.first)
            }
    }

    private fun markResolved(address: Int) {
        unresolved.remove(address)
    }
}


class Assembly(private val rom: ByteBuffer, private val start: Int = 0) {
    init {
        rom.position(start)
    }

    val position: Int
        get() = rom.position()

    fun write(machineCode: ByteArray) {
        rom.put(machineCode)
    }

    fun toByteArray(): ByteArray {
        rom.flip()
        rom.position(start)
        val bytes = ByteArray(rom.remaining())
        rom.get(bytes)
        return bytes
    }

    operator fun set(index: Int, word: Word) {
        this[index] = word.msb
        this[index + 1] = word.lsb
    }

    operator fun get(index: Int): Byte = rom.get(index)

    operator fun set(index: Int, byte: Byte) {
        rom.put(index, byte)
    }

    companion object {
        fun allocate(size: Int, base: Int) = Assembly(ByteBuffer.allocate(size), base)
    }
}

fun Assembly.printAsHex(upperCase: Boolean = true): String {
    return toByteArray().joinToString("") {
        "%02x".format(it).let { hex -> if (upperCase) hex.toUpperCase() else hex }
    }
}


class Arguments(
    private val args: Iterator<String>,
    private val offset: Int,
    private val symbolTable: SymbolTable
) {
    fun literal(symbol: String): String {
        return next().let {
            if (symbol == it) symbol else throw IllegalArgumentException("expected literal `$symbol`, not: $it")
        }
    }

    fun register(): String {
        return next().let {
            Regex("V(?<number>$HEX)").matchEntire(it)?.let { match ->
                val (number, _) = match.destructured
                number
            } ?: throw IllegalArgumentException("invalid register: $it")
        }
    }

    fun address(): String {
        next().let {
            Regex("$HEX{1,3}").matchEntire(it)?.value?.let { address ->
                return address.padStart(3, '0')
            }

            if (it in symbolTable) return symbolTable[it].toString(16)

            return symbolTable.unresolved(offset, it)
        }
    }

    fun nibbles(count: Int): String {
        next().let {
            Regex("$HEX{1,$count}").matchEntire(it)?.value?.let { hex ->
                return hex.padStart(count, '0')
            }
            Regex("$BIT{${count.times(4)}}").matchEntire(it)?.value?.let { bits ->
                return bits.replace(".", "0").toInt(radix = 2).toString(16)
            }

            throw IllegalArgumentException("expected $count nibble(s), not $it")
        }
    }

    private fun next() = args.next()

    companion object {
        private val HEX = Regex("[0-9a-fA-F]")
        private val BIT = Regex("[0.1]")

        operator fun invoke(args: List<String>, address: Int, symbolTable: SymbolTable) =
            Arguments(args.iterator(), address, symbolTable)
    }
}


private fun <T> Result<T>.asSequence() = fold(onSuccess = { sequenceOf(it) }, onFailure = { sequenceOf() })
