package com.vtence.chip8

object Disassembler {
    fun disassemble(assembly: Assembly, symbolTable: SymbolTable): Program {
        val statements = assembly.readRemaining().map {
            val (instruction, args) = disassemble(it, symbolTable)
            AssemblyCode(instruction.mnemonic, args.toList())
        }

        return Program(statements.toList())
    }

    private fun disassemble(opcode: OpCode, symbolTable: SymbolTable): Pair<Instruction, Arguments> {
        val instruction = InstructionsTable.lookup(opcode)
        val args = instruction.disassemble(opcode, Arguments(listOf(), 0, symbolTable))
        return Pair(instruction, args)
    }
}

fun disassemble(rom: ByteArray): Program {
    val base = 0x200
    val symbolTable = SymbolTable(base)

    return Disassembler.disassemble(Assembly.load(rom, base), symbolTable)
}
