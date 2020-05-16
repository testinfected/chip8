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
import com.vtence.chip8.Operand.Companion.word


data class Instruction(val opcode: OpCode, val mnemonic: String, val operands: List<Operand>) {
    val arity = operands.size

    fun write(assembly: Assembly, arguments: Arguments) {
        assembly.write(
            operands.fold(opcode) { instruction, operand -> operand.assemble(instruction, arguments) }.toByteArray()
        )
    }

    companion object {
        fun op(opcode: String, mnemonic: String, vararg operands: Operand) =
            Instruction(OpCode(opcode), mnemonic, operands.toList())
    }
}


class OpCode(private val opcode: String) {
    fun replace(symbol: String, value: String) = OpCode(opcode.replace(symbol, value))

    fun replace(pattern: Regex, value: String) = OpCode(opcode.replace(pattern, value))

    fun toByteArray() = opcode.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}


sealed class Operand {
    abstract fun assemble(opcode: OpCode, args: Arguments): OpCode

    companion object {
        val addr = Address("nnn")

        val word = ImmediateValue("n", 4)

        val byte = ImmediateValue("k", 2)

        val nibble = ImmediateValue("n", 1)

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

class ImmediateValue(private val symbol: String, private val nibbles: Int) : Operand() {

    override fun assemble(opcode: OpCode, args: Arguments): OpCode {
        return opcode.replace(Regex("$symbol{$nibbles}"), args.nibbles(nibbles))
    }
}

class Address(private val symbol: String) : Operand() {

    override fun assemble(opcode: OpCode, args: Arguments): OpCode {
        return opcode.replace(symbol, args.address())
    }
}

class Register(private val symbol: String) : Operand() {

    override fun assemble(opcode: OpCode, args: Arguments): OpCode {
        return opcode.replace(symbol, args.register())
    }
}

class Literal(private val symbol: String) : Operand() {

    override fun assemble(opcode: OpCode, args: Arguments): OpCode {
        args.literal(symbol)
        return opcode
    }
}


// See http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#3.1

val INSTRUCTIONS_SET = sequenceOf(
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
    op("Fx07", "LD", Vx, DT),
    op("Fx0A", "LD", Vx, K),
    op("Fx15", "LD", DT, Vx),
    op("Fx18", "LD", ST, Vx),
    op("Fx1E", "ADD", I, Vx),
    op("Fx29", "LD", F, Vx),
    op("Fx33", "LD", B, Vx),
    op("Fx55", "LD", `(I)`, Vx),
    op("Fx65", "LD", Vx, `(I)`),
    op("kk", "BYTE", byte),
    op("nnnn", "WORD", word)
)


