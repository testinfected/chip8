package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class DisassemblerTest {

    @Test
    fun `an empty program`() {
        val assembly = assemble("""""")

        val program = disassemble(assembly.rom());

        assertThat("lines", program.lines, List<String>::isEmpty)
    }

    @Test
    fun `a single instruction`() {
        val assembly = assemble(
            """
            CLS
        """.trimIndent()
        )

        val program = disassemble(assembly.rom());

        assertThat("lines", program.lines, List<String>::containsAll, listOf("CLS"))
    }

    @Test
    fun `each instruction`() {
        val sourceCode = """
            CLS
            RET
            SYS 400
            JP 200
            CALL 300
        """.trimIndent()

        val program = disassemble(assemble(sourceCode).rom());

        assertThat("lines", program.lines, equalTo(sourceCode.lines()))
    }
}