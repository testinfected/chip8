package com.vtence.chip8

import com.vtence.chip8.Instruction.Companion.op
import com.vtence.chip8.Operand.Companion.B
import com.vtence.chip8.Operand.Companion.DT
import com.vtence.chip8.Operand.Companion.F
import com.vtence.chip8.Operand.Companion.I
import com.vtence.chip8.Operand.Companion.K
import com.vtence.chip8.Operand.Companion.ST
import com.vtence.chip8.Operand.Companion.V0
import com.vtence.chip8.Operand.Companion.Vx
import com.vtence.chip8.Operand.Companion.Vy
import com.vtence.chip8.Operand.Companion.`(I)`
import com.vtence.chip8.Operand.Companion.addr
import com.vtence.chip8.Operand.Companion.byte
import com.vtence.chip8.Operand.Companion.nibble


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
        val addr = Nibbles("n", 3)

        val byte = Nibbles("k", 2)

        val nibble = Nibbles("n", 1)

        val B = Literal("B")

        val F = Literal("F")

        val I = Literal("I")

        val K = Literal("K")

        val DT = Literal("DT")

        val ST = Literal("ST")

        val `(I)` = Literal("[I]")

        val V0 = Literal("V0")

        val Vx = V("x")

        val Vy = V("y")

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


class Literal(private val symbol: String): Operand() {

    override fun replace(opcode: String, value: String): String {
        validate(value)
        return opcode
    }

    private fun validate(operand: String) {
        if (symbol != operand) throw IllegalArgumentException("expected literal $symbol, got $operand")
    }
}


// See http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#3.1

val INSTRUCTIONS_TABLE = sequenceOf(
    op("000E", "CLS"),
    op("00EE", "RET"),
    op("0nnn", "SYS", addr),
    op("1nnn", "JP", addr),
    op("2nnn", "CALL", addr),
    op("3xkk", "SE", Vx, byte),
    op("4xkk", "SNE", Vx, byte),
    op("5xy0", "SE", Vx, Vy),
    op("6xkk", "LD", Vx, byte),
    op("7xkk", "ADD", Vx, byte),
    op("8xy0", "LD", Vx, Vy),
    op("8xy1", "OR", Vx, Vy),
    op("8xy2", "AND", Vx, Vy),
    op("8xy3", "XOR", Vx, Vy),
    op("8xy4", "ADD", Vx, Vy),
    op("8xy5", "SUB", Vx, Vy),
    // See http://mattmik.com/files/chip8/mastering/chip8.html note on 8xy6
    op("8xy6", "SHR", Vx, Vy),
    op("8xy7", "SUBN", Vx, Vy),
    // See http://mattmik.com/files/chip8/mastering/chip8.html note on 8xyE
    op("8xyE", "SHL", Vx, Vy),
    op("9xy0", "SNE", Vx, Vy),
    op("Annn", "LD", I, addr),
    op("Bnnn", "JP", V0, addr),
    op("Cxkk", "RND", Vx, byte),
    op("Dxyn", "DRW", Vx, Vy, nibble),
    op("Ex9E", "SKP", Vx),
    op("ExA1", "SKNP", Vx),
    op("Fx07","LD", Vx, DT),
    op("Fx0A", "LD", Vx, K),
    op("Fx15", "LD", DT, Vx),
    op("Fx18", "LD", ST, Vx),
    op("Fx1E", "ADD", I, Vx),
    op("Fx29" , "LD", F, Vx),
    op("Fx33" , "LD", B, Vx),
    op("Fx55" , "LD", `(I)`, Vx),
    op("Fx65", "LD", Vx, `(I)`)
)


