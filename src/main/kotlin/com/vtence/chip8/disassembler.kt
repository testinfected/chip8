package com.vtence.chip8

object Disassembler {
    fun disassemble(assembly: Assembly, symbolTable: SymbolTable): Program {
        val statements = mutableListOf<Statement>()

        while (assembly.hasRemaining()) {
            val opcode = assembly.read()
            val (instruction, args) = disassemble(opcode, symbolTable)
            statements.add(AssemblyCode(instruction.mnemonic, args.toList()))
        }

        return Program(statements)
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
