package com.vtence.chip8


object Assembler {

    private const val base = 0x200
    private val symbols = SymbolTable(base)

    fun assemble(program: Program): Assembly {
        val assembly = Assembly.allocate(0x1000, base = base)
        assemble(program, into = assembly)
        resolveSymbols(assembly)
        return assembly
    }

    private fun assemble(program: Program, into: Assembly) {
        program.forEach { statement ->
            when (statement) {
                is AssemblyCode -> assemble(statement, into)
                is LabelDefinition -> defineLabel(statement, into)
                is Comment -> {
                }
                BlankLine -> {
                }
            }
        }
    }

    private fun resolveSymbols(assembly: Assembly) {
        symbols.resolve { (offset: Int, address: Word) ->
            assembly[offset] = Word(assembly[offset].high or address.msb.low, address.lsb)
        }
    }

    private fun assemble(code: AssemblyCode, into: Assembly) {
        val opcode = InstructionsTable
            .list(code.mnemonic, arity = code.operands.size)
            .asSequence()
            .map { runCatching { it.assemble(code.assemble(into.position, symbols)) } }
            .firstOrNull { it.isSuccess }
            ?: throw SyntaxException(code.toString())

        opcode.onSuccess(into::write)
    }

    private fun defineLabel(statement: LabelDefinition, into: Assembly) {
        symbols[statement.label] = into.position.toWord()
    }

    private fun AssemblyCode.assemble(position: Int, symbols: SymbolTable) = Arguments(operands, position, symbols)
}

fun assemble(sourceCode: String) = Assembler.assemble(Program.fromSource(sourceCode))