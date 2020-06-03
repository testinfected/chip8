package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class DisassemblerTest {

    @Test
    fun `an empty program`() {
        val assembly = assemble("""""")

        val program = Disassembler.disassemble(assembly.rom());

        assertThat("lines", program.lines, List<String>::isEmpty)
    }

    @Test
    @Disabled("pending")
    fun `a single instruction`() {
        val assembly = assemble(
            """
            CLS
        """.trimIndent()
        )

        val program = Disassembler.disassemble(assembly.rom());

        assertThat("lines", program.lines, List<String>::containsAll, listOf("CLS"))
    }
}