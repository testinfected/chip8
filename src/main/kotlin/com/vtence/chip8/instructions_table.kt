package com.vtence.chip8

import com.vtence.chip8.Instruction.Companion.op
import java.nio.ByteBuffer


val ADDRESS_PATTERN = Regex("nnn")


data class Instruction(val opcode: String, val mnemonic: String) {
    fun compile(output: ByteBuffer, operands: Iterable<String>) {
        val replacements = operands.iterator()

        output.put(opcode.replace(ADDRESS_PATTERN) { replacements.next() }
            .toHex())
    }

    companion object {
        fun op(opcode: String, mnemonic: String) = Instruction(opcode, mnemonic)
    }
}

val INSTRUCTIONS_TABLE = listOf(
    op("000E", "CLS"),
    op("00EE", "RET"),
    op("0nnn", "SYS"),
    op("1nnn", "JP"),
    op("2nnn", "CALL")
)


fun String.toHex(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun ByteArray.toHex() = joinToString("") { "%02X".format(it) }


