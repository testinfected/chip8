package com.vtence.chip8

import com.vtence.chip8.Instruction.Companion.op
import com.vtence.chip8.Operand.Companion.V
import com.vtence.chip8.Operand.Companion.addr
import com.vtence.chip8.Operand.Companion.byte
import com.vtence.chip8.Operand.Companion.nibble
import com.vtence.chip8.Operand.Companion.x
import com.vtence.chip8.Operand.Companion.y


private val HEX = Regex("[0-9a-fA-F]")


data class Instruction(val opcode: String, val mnemonic: String, val operands: List<Operand>) {
    val arity = operands.size

    fun compile(arguments: Collection<String>): ByteArray {
        if (arity != arguments.size) throw IllegalArgumentException("expected $arity arguments, got ${arguments.size}")

        return operands.zip(arguments)
            .fold(opcode) { instruction, (operand, argument) -> operand.replace(instruction, argument) }
            .toHex()
    }

    companion object {
        fun op(opcode: String, mnemonic: String, vararg operands: Operand) =
            Instruction(opcode, mnemonic, operands.toList())
    }
}


sealed class Operand {
    abstract fun replace(opcode: String, value: String): String

    companion object {
        const val x = "x"
        const val y = "y"

        fun addr() = Nibbles("n", 3)

        fun byte() = Nibbles("k", 2)

        fun nibble() = Nibbles("n", 1)

        fun V(symbol: String) = Register(symbol)
    }
}

class Nibbles(private val symbol: String, private val count: Int): Operand() {

    override fun replace(opcode: String, value: String): String {
        return opcode.replace(Regex("$symbol{$count}"), parse(value))
    }

    private fun parse(operand: String): String {
        return Regex("$HEX{$count}").matchEntire(operand)?.value
            ?: throw IllegalArgumentException("expected $count nibble(s), not $operand")
    }
}


class Register(private val symbol: String) : Operand() {

    override fun replace(opcode: String, value: String): String {
        return opcode.replace(symbol, parse(value))
    }

    private fun parse(operand: String): String {
        return Regex("V(?<number>$HEX)").matchEntire(operand)?.let {
            val (number, _) = it.destructured
            number
        } ?: throw IllegalArgumentException("invalid register: $operand")
    }
}


// See http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#3.1

val INSTRUCTIONS_TABLE = sequenceOf(
    op("000E", "CLS"),
    op("00EE", "RET"),
    op("0nnn", "SYS", addr()),
    op("1nnn", "JP", addr()),
    op("2nnn", "CALL", addr()),
    op("3xkk", "SE", V(x), byte()),
    op("4xkk", "SNE", V(x), byte()),
    op("5xy0", "SE", V(x), V(y)),
    op("6xkk", "LD", V(x), byte()),
    op("7xkk", "ADD", V(x), byte()),
    op("8xy0", "LD", V(x), V(y)),
    op("8xy1", "OR", V(x), V(y)),
    op("8xy2", "AND", V(x), V(y)),
    op("8xy3", "XOR", V(x), V(y)),
    op("8xy4", "ADD", V(x), V(y)),
    op("8xy5", "SUB", V(x), V(y)),
    // See http://mattmik.com/files/chip8/mastering/chip8.html note on 8xy6
    op("8xy6", "SHR", V(x), V(y)),
    op("8xy7", "SUBN", V(x), V(y)),
    // See http://mattmik.com/files/chip8/mastering/chip8.html note on 8xyE
    op("8xyE", "SHL", V(x), V(y)),
    op("9xy0", "SNE", V(x), V(y)),
    op("Cxkk", "RND", V(x), byte()),
    op("Dxyn", "DRW", V(x), V(y), nibble()),
    op("Ex9E", "SKP", V(x)),
    op("ExA1", "SKNP", V(x))
)


