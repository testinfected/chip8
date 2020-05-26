package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test

class DisassemblerTest {

    @Test
    fun `an empty program`() {
        val assembly = assemble("""""")

        val program = Disassembler.disassemble(assembly);

        assertThat("lines", program.lines, List<String>::isEmpty)
    }

    @Test
    fun `a single instruction`() {
        val assembly = assemble(
            """
            CLS
        """.trimIndent()
        )

        val program = Disassembler.disassemble(assembly);

        assertThat("lines", program.lines, List<String>::containsAll, listOf("CLS"))
    }
}