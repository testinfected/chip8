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
        symbolTable.resolve { (offset: Int, address: Word) ->
            assembly[offset] = Word(assembly[offset].high or address.msb.low, address.lsb)
        }
    }

    private fun assemble(code: AssemblyCode, into: Assembly) {
        val opcode = InstructionsTable
            .list(code.mnemonic, arity = code.operands.size)
            .map { runCatching { it.assemble(Arguments(code.operands, into.position, symbolTable)) } }
            .flatMap { it.asSequence() }
            .firstOrNull()
            ?: throw SyntaxException(code.toString())

        into.write(opcode)
    }

    private fun defineLabel(statement: LabelDefinition, into: Assembly) {
        symbolTable[statement.label] = into.position.toWord()
    }
}

fun assemble(sourceCode: String) = Assembler.assemble(Program.fromSource(sourceCode))


class SymbolTable(private val defaultAddress: Word) {
    private val resolved = mutableMapOf<String, Word>()
    private val unresolved = mutableMapOf<Int, String>()

    operator fun set(label: String, address: Word) {
        if (label in resolved) throw IllegalArgumentException("label `$label` already defined")
        resolved[label] = address
    }

    operator fun contains(symbol: String) = symbol in resolved

    operator fun get(symbol: String): Word =
        resolved[symbol] ?: throw IllegalArgumentException("symbol `$symbol` not found")

    fun unresolved(atOffset: Int, symbol: String): Word {
        unresolved[atOffset] = symbol
        return defaultAddress
    }

    fun resolve(action: (Pair<Int, Word>) -> Unit) {
        unresolved
            .map { (address, symbol) -> address to get(symbol) }
            .forEach {
                action(it)
                markResolved(it.first)
            }
    }

    private fun markResolved(offset: Int) {
        unresolved.remove(offset)
    }

    companion object {
        operator fun invoke(defaultAddress: Int) = SymbolTable(defaultAddress.toWord())
    }
}


class Assembly(private val rom: ByteBuffer, private val base: Int) {
    init {
        rom.position(base)
    }

    val position: Int
        get() = rom.position()

    fun write(opcode: OpCode) {
        rom.put(opcode.toByteArray())
    }

    fun hasRemaining() = rom.hasRemaining()

    fun read() = OpCode(Word(rom.get(), rom.get()))

    operator fun get(index: Int): Byte = rom.get(index)

    operator fun set(index: Int, byte: Byte) {
        rom.put(index, byte)
    }

    operator fun set(index: Int, word: Word) {
        this[index] = word.msb
        this[index + 1] = word.lsb
    }

    fun rom(): ByteArray {
        rom.flip()
        rom.position(base)
        val bytes = ByteArray(rom.remaining())
        rom.get(bytes)
        return bytes
    }

    companion object {
        fun allocate(size: Int, base: Int) = Assembly(ByteBuffer.allocate(size), base)

        fun load(rom: ByteArray, base: Int): Assembly {
            val buffer = ByteBuffer.allocate(base + rom.size)
            buffer.position(base)
            buffer.put(rom)
            return Assembly(buffer, base)
        }
    }
}

fun print(assembly: Assembly, upperCase: Boolean = true): String {
    return assembly.rom().joinToString("") {
        "%02x".format(it).let { hex -> if (upperCase) hex.toUpperCase() else hex }
    }
}


class Arguments(
    private val args: Iterator<String>,
    private val offset: Int,
    private val symbolTable: SymbolTable
) {
    fun literal(symbol: String): String {
        return args.next().let {
            if (symbol == it) symbol else throw IllegalArgumentException("expected literal `$symbol`, not: $it")
        }
    }

    fun register(): Byte {
        return args.next().let {
            Regex("V(?<number>$HEX)").matchEntire(it)?.let { match ->
                val (number, _) = match.destructured
                number.toWord().lsb
            } ?: throw IllegalArgumentException("invalid register: $it")
        }
    }

    fun address(): Word {
        args.next().let {
            Regex("$HEX{1,3}").matchEntire(it)?.value?.let { address ->
                return address.toWord()
            }

            if (it in symbolTable) return symbolTable[it]

            return symbolTable.unresolved(offset, it)
        }
    }

    fun nibbles(count: Int): Word {
        args.next().let {
            Regex("$HEX{1,$count}").matchEntire(it)?.value?.let { nibbles ->
                return nibbles.toWord()
            }
            Regex("$BIT{${count.times(4)}}").matchEntire(it)?.value?.let { bits ->
                return bits.replace(".", "0").toInt(radix = 2).toWord()
            }

            throw IllegalArgumentException("expected $count nibble(s), not $it")
        }
    }

    fun addAddress(address: String): Arguments {
        return Arguments(toList() + address, offset, symbolTable)
    }

    fun toList() = args.asSequence().toList()

    companion object {
        private val HEX = Regex("[0-9a-fA-F]")
        private val BIT = Regex("[0.1]")

        operator fun invoke(args: List<String>, address: Int, symbolTable: SymbolTable) =
            Arguments(args.iterator(), address, symbolTable)
    }
}


private fun <T> Result<T>.asSequence() = fold(onSuccess = { sequenceOf(it) }, onFailure = { sequenceOf() })
