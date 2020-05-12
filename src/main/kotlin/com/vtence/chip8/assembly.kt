package com.vtence.chip8

import java.io.Reader
import java.io.StringReader
import java.nio.ByteBuffer

class Program(private val statements: List<Statement>) {

    fun assemble(): Assembly {
        val assembly = Assembly.allocate(4096)
        statements.forEach { it.writeTo(assembly) }
        return assembly
    }

    companion object {
        fun read(source: Reader) = Program(source.readLines().map { parse(it) })

        fun source(assemblyCode: String): Program = read(StringReader(assemblyCode))
    }
}


class Assembly(private val rom: ByteBuffer) {
    fun write(machineCode: ByteArray) {
        rom.put(machineCode)
    }

    fun toByteArray(): ByteArray {
        rom.flip()
        return ByteArray(rom.remaining()).also { rom.get(it) }
    }

    fun args(operands: List<String>) = Arguments(operands)

    companion object {
        fun allocate(size: Int) = Assembly(ByteBuffer.allocate(size))
    }
}

fun Assembly.printAsHex(upperCase: Boolean = true): String {
    return toByteArray().joinToString("") {
        "%02x".format(it) .let { hex -> if (upperCase) hex.toUpperCase() else hex }
    }
}


class Arguments(private val args: Iterator<String>) {
    fun literal(symbol: String): String {
        val next = next()
        return if (symbol == next) symbol else throw IllegalArgumentException("expected literal $symbol, got $next")
    }

    fun register(): String {
        val next = next()
        return Regex("V(?<number>$HEX)").matchEntire(next)?.let {
            val (number, _) = it.destructured
            number
        } ?: throw IllegalArgumentException("invalid register: $next")
    }

    fun nibbles(count: Int): String {
        val next = next()
        return Regex("$HEX{1,$count}").matchEntire(next)?.value?.padStart(count,'0')
            ?: throw IllegalArgumentException("expected $count nibble(s), not $next")
    }

    private fun next() = args.next()

    companion object {
        private val HEX = Regex("[0-9a-fA-F]")

        operator fun invoke(args: List<String>) = Arguments(args.iterator())
    }
}

