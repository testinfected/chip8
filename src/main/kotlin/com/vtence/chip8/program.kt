package com.vtence.chip8

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.Reader
import java.io.StringReader

class Program(private val statements: List<Statement>) {

    fun assembleTo(output: OutputStream) {
        statements.forEach { it.assembleTo(output) }
    }

    companion object {
        fun read(source: Reader) = Program(source.readLines().map { parse(it) })

        fun source(assemblyCode: String): Program = read(StringReader(assemblyCode))
    }
}


fun Program.assemble() = assemble { it }

fun <T> Program.assemble(format: (bytes: ByteArray) -> T): T  {
    val output = ByteArrayOutputStream()
    assembleTo(output)
    val actual = output.toByteArray()
    return format(actual)
}
