package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InstructionsTableTest {

    private val validInstructions = listOf(
        "CLS" to "000E",
        "RET" to "00EE",
        "SYS 5AF" to "05AF",
        "JP 200" to "1200",
        "CALL A50" to "2A50",
        "SE V8, EF" to "38EF",
        "SNE V5, 8B" to "458B",
        "SE V0, V1" to "5010",
        "LD V3, 5D" to "635D",
        "ADD V7, 7A" to "777A",
        "LD V1, V2" to "8120",
        "OR V8, VE" to "88E1",
        "AND V4, V9" to "8492",
        "XOR V0, VC" to "80C3",
        "ADD V1, V6" to "8164",
        "SUB VE, VD" to "8ED5",
        "SHR V1, V2" to "8126",
        "SUBN V2, V3" to "8237",
        "SHL V8, VE" to "88EE",
        "SNE V3, V1" to "9310",
        // Annn
        // Bnnn
        "RND V1, 77" to "C177",
        "DRW V1, V3, 5" to "D135",
        "SKP V2" to "E29E",
        "SKNP V3" to "E3A1"
        // F...
    )

    @Test
    fun `compiles valid statements to binary code`() {
        for ((statement, machineCode) in validInstructions) {
            assertThat(
                parse(statement).compile { it.toHex(upper = true) },
                equalTo(machineCode), { "$statement, once compiled" })
        }
    }

    private val invalidInstructions = listOf(
        "ABC",
        "CLS 200",
        "JP 10",
        "CALL V5RF",
        "SE R8, EF",
        "SE VAB, EF"
    )

    @Test
    fun `rejects invalid statements`() {
        invalidInstructions.forEach {
            assertThrows<SyntaxException>(it) { parse(it).compile() }
        }
    }
}