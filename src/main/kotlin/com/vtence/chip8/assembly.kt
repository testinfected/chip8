package com.vtence.chip8

import java.io.Reader
import java.io.StringReader
import java.nio.ByteBuffer

class Program(private val statements: List<Statement>) {

    fun assemble(): Assembly {
        val assembly = Assembly.allocate(0x1000, base = 0x200)
        statements.forEach { it.writeTo(assembly) }
        assembly.resolve()
        return assembly
    }

    companion object {
        fun read(source: Reader) = Program(source.readLines().map { parse(it) })

        fun source(assemblyCode: String): Program = read(StringReader(assemblyCode))
    }
}


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
            .forEach(action)
    }
}


class Assembly(private val rom: ByteBuffer, private val start: Int = 0) {
    private val symbolTable = SymbolTable(0x200)

    init {
        rom.position(start)
    }

    fun write(machineCode: ByteArray) {
        rom.put(machineCode)
    }

    operator fun set(index: Int, word: Word) {
        rom.put(index, word.msb)
        rom.put(index + 1, word.lsb)
    }

    fun toByteArray(): ByteArray {
        rom.flip()
        rom.position(start)
        val bytes = ByteArray(rom.remaining())
        rom.get(bytes)
        return bytes
    }

    fun mark(label: String) {
        symbolTable[label] = rom.position()
    }

    fun args(operands: List<String>) = Arguments(operands, rom.position(), symbolTable)

    fun resolve() {
        symbolTable.resolve { (offset: Int, address: Int) ->
            this[offset] = Word(rom.get(offset).high or address.msb.low, address.lsb)
        }
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
        return next().let {
            Regex("$HEX{1,3}").matchEntire(it)?.value?.let { address ->
                return address.padStart(3, '0')
            }

            if (it in symbolTable) return symbolTable[it].toString(16)

            symbolTable.unresolved(offset, it)
        }
    }

    fun nibbles(count: Int): String {
        return next().let {
            Regex("$HEX{1,$count}").matchEntire(it)?.value?.padStart(count, '0')
                ?: throw IllegalArgumentException("expected $count nibble(s), not $it")
        }
    }

    private fun next() = args.next()

    companion object {
        private val HEX = Regex("[0-9a-fA-F]")

        operator fun invoke(args: List<String>, address: Int, symbolTable: SymbolTable) =
            Arguments(args.iterator(), address, symbolTable)
    }
}