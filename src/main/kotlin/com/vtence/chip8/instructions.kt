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
import java.lang.IllegalArgumentException


class Instruction(private val pattern: OpCode, val mnemonic: String, private val operands: List<Operand>) {
    val arity = operands.size

    fun assemble(args: Arguments): OpCode {
        return operands.fold(pattern) { result, operand -> operand.encode(args, into = result) }
    }

    fun disassemble(code: OpCode, into: Arguments): Arguments {
       return operands.fold(into) { result, arg -> arg.decode(from = code, result, template = pattern) }
    }

    fun matches(other: OpCode) = pattern.matches(other)

    companion object {
        fun op(pattern: String, mnemonic: String, vararg operands: Operand) =
            Instruction(OpCode(pattern), mnemonic, operands.toList())
    }
}


class OpCode(private val code: String) {
    private val pattern = Regex(code.replace(Regex("[nxyk]"), "[0-F]"))

    fun pack(into: String, value: String) = OpCode(code.replace(into, value))

    fun pack(into: Regex, value: String) = OpCode(code.replace(into, value))

    fun unpack(from: OpCode, symbol: String) = unpack(from, Regex(symbol))

    fun unpack(from: OpCode, symbol: Regex): String {
        return symbol.find(code)?.let {
            from.code.substring(it.range)
        } ?: throw IllegalArgumentException("$symbol not found in $code")
    }

    fun matches(other: OpCode) = pattern.matches(other.code.toUpperCase())

    override fun toString() = code

    fun toByteArray() = code.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    companion object {
        operator fun invoke(byte: Byte) = OpCode(byte.toHex(upperCase = true))

        operator fun invoke(msb: Byte, lsb: Byte) = OpCode(Word(msb, lsb))

        operator fun invoke(word: Word) = OpCode(word.toHex(upperCase = true))
    }
}


sealed class Operand {
    abstract fun encode(args: Arguments, into: OpCode): OpCode

    abstract fun decode(from: OpCode, args: Arguments, template: OpCode): Arguments

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

    override fun encode(args: Arguments, into: OpCode): OpCode {
        return into.pack(into = Regex("$symbol{$nibbles}"), args.popNibbles(nibbles).toHex().takeLast(nibbles))
    }

    override fun decode(from: OpCode, args: Arguments, template: OpCode): Arguments {
        return args.pushValue(template.unpack(from, Regex("$symbol{$nibbles}")))
    }
}

class Address(private val symbol: String) : Operand() {

    override fun encode(args: Arguments, into: OpCode): OpCode {
        return into.pack(symbol, args.popAddress().toHex().takeLast(symbol.length))
    }

    override fun decode(from: OpCode, args: Arguments, template: OpCode): Arguments {
        return args.pushAddress(template.unpack(from, symbol))
    }
}

class Register(private val symbol: String) : Operand() {

    override fun encode(args: Arguments, into: OpCode): OpCode {
        return into.pack(symbol, args.popRegister().toHex().takeLast(symbol.length))
    }

    override fun decode(from: OpCode, args: Arguments, template: OpCode): Arguments {
        return args.pushRegister(template.unpack(from, symbol))
    }
}

class Literal(private val symbol: String) : Operand() {

    override fun encode(args: Arguments, into: OpCode): OpCode {
        args.popLiteral(symbol)
        return into
    }

    override fun decode(from: OpCode, args: Arguments, template: OpCode): Arguments {
        return args.pushValue(symbol)
    }
}


object InstructionsTable {
    fun list(mnemonic: String, arity: Int): List<Instruction> {
        return instructionSet
            .filter { it.mnemonic == mnemonic }
            .filter { it.arity == arity }
    }

    fun lookup(opcode: OpCode): Instruction {
        return instructionSet.first { it.matches(opcode) }
    }

    // See http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#3.1
    private val instructionSet = listOf(
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
}


