package com.vtence.chip8

object Disassembler {
    fun disassemble(assembly: Assembly, symbols: SymbolTable): Program {
        val statements = assembly.read().map { opcode: OpCode ->
            val (instruction, args) = disassemble(opcode, symbols)
            AssemblyCode(instruction.mnemonic, args.toList())
        }

        return Program(statements.toList())
    }

    private fun disassemble(opcode: OpCode, symbols: SymbolTable): Pair<Instruction, Arguments> {
        val instruction = InstructionsTable.lookup(opcode)
        val args = instruction.disassemble(opcode, into = Arguments.empty(using = symbols))
        return Pair(instruction, args)
    }
}


fun disassemble(rom: ByteArray, base: Int = 0x200): Program {
    return Disassembler.disassemble(Assembly.load(rom, base), symbols = SymbolTable(base))
}
