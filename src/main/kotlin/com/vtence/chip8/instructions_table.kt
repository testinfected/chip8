package com.vtence.chip8

import com.vtence.chip8.Instruction.Companion.op
import java.lang.IllegalArgumentException


val ADDRESS = Regex("nnn")
val TARGET = Regex("x")
val SOURCE = Regex("y")
val BYTE = Regex("kk")
val NIBBLE = Regex("n")

val HEX = Regex("[0-9a-fA-F]")
val REGISTER = Regex("V(?<register>$HEX)")

private fun nibbles(count: Int) = Regex("$HEX{$count}")


data class Instruction(val opcode: String, val mnemonic: String) {
    fun compile(operands: Iterable<String>): ByteArray {
        val replacements = operands.iterator()

        val code = opcode
            .replace(TARGET) { register(replacements.next()) }
            .replace(SOURCE) { register(replacements.next()) }
            .replace(ADDRESS) { value(replacements.next(), digits = 3) }
            .replace(BYTE) { value(replacements.next(), digits = 2) }
            .replace(NIBBLE) { value(replacements.next(), digits = 1) }
            .toHex()

        if (replacements.hasNext()) throw IllegalArgumentException(replacements.next())

        return code
    }

    private fun value(operand: String, digits: Int): String {
        return nibbles(digits).matchEntire(operand)?.value
            ?: throw IllegalArgumentException("expected $digits nibble(s), not $operand")
    }

    private fun register(operand: String): String {
        return REGISTER.matchEntire(operand)?.let {
            val (register, _) =  it.destructured
            register
        } ?: throw IllegalArgumentException("invalid register: $operand")
    }

    companion object {
        fun op(opcode: String, mnemonic: String) = Instruction(opcode, mnemonic)
    }
}

// See http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#3.1

val INSTRUCTIONS_TABLE = sequenceOf(
    op("000E", "CLS"),
    op("00EE", "RET"),
    op("0nnn", "SYS"),
    op("1nnn", "JP"),
    op("2nnn", "CALL"),
    op("3xkk", "SE"),
    op("4xkk", "SNE"),
    op("5xy0", "SE"),
    op("6xkk", "LD"),
    op("7xkk", "ADD"),
    op("8xy0", "LD"),
    op("8xy1", "OR"),
    op("8xy2", "AND"),
    op("8xy3", "XOR"),
    op("8xy4", "ADD"),
    op("8xy5", "SUB"),
    op("8xy6", "SHR"), // See http://mattmik.com/files/chip8/mastering/chip8.html note on 8xy6 and 8xyE
    op("8xy7", "SUBN"),
    op("8xyE", "SHL"),
    op("9xy0", "SNE"),
    op("Cxkk", "RND"),
    op("Dxyn", "DRW"),
    op("Ex9E", "SKP"),
    op("ExA1", "SKNP")
)


