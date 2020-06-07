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
    fun `entire instruction set`() {
        val sourceCode = """
            CLS
            RET
            SYS 400
            JP 200
            CALL 300
            SE V1, 35
            SNE V5, 8B
            SE V0, V1
            LD V3, 5D
            ADD V7, 01
            ADD V5, FA
            LD V1, V2
            OR V8, VE
            AND V4, V9
            XOR V0, VC
            ADD V1, V6
            SUB VE, VD
            SHR V1, V2
            SUBN V2, V3
            SHL V8, VE
            SNE V3, V1
            LD I, 300
            JP V0, 500
            RND V1, 77
            DRW V1, V3, 5
            SKP V2
            SKNP V3
            LD V1, DT
            LD V2, K
            LD DT, V3
            LD ST, V4
            ADD I, V5
            LD F, V6
            LD B, V7
            LD [I], V8
            LD V9, [I]
        """.trimIndent()

        val program = disassemble(assemble(sourceCode).rom());

        assertThat("lines", program.lines, equalTo(sourceCode.lines()))
    }

    @Test
    fun `data, seen as instructions`() {
        val assembly = assemble(
            """
            WORD 14EF
        """.trimIndent()
        )

        val program = disassemble(assembly.rom());

        assertThat("lines", program.lines, List<String>::containsAll, listOf(
            "JP 4EF"
        ))
    }

    @Test
    fun `data, not instructions`() {
        val assembly = assemble(
            """
            WORD EF04
            BYTE  CD
        """.trimIndent()
        )

        val program = disassemble(assembly.rom());

        assertThat("lines", program.lines, List<String>::containsAll, listOf(
            "WORD EF04",
            "BYTE CD"
        ))
    }
}