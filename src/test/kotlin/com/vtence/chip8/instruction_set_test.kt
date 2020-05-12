package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InstructionSetTest {

    private val validInstructions = listOf(
        "CLS" to "000E",
        "RET" to "00EE",
        "SYS 5AF" to "05AF",
        "JP 200" to "1200",
        "JP 20" to "1020", // same with less digits
        "CALL A50" to "2A50",
        "SE V8, EF" to "38EF",
        "SNE V5, 8B" to "458B",
        "SE V0, V1" to "5010",
        "LD V3, 5D" to "635D",
        "ADD V7, 1" to "7701", // with a single digit this time
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
        "LD I, 300" to "A300",
        "JP V0, 500" to "B500",
        "RND V1, 77" to "C177",
        "DRW V1, V3, 5" to "D135",
        "SKP V2" to "E29E",
        "SKNP V3" to "E3A1",
        "LD V1, DT" to "F107",
        "LD V2, K" to "F20A",
        "LD DT, V3" to "F315",
        "LD ST, V4" to "F418",
        "ADD I, V5" to "F51E",
        "LD F, V6" to "F629",
        "LD B, V7" to "F733",
        "LD [I], V8" to "F855",
        "LD V9, [I]" to "F965"
    )

    @Test
    fun `compiles valid statements to binary code`() {
        for ((statement, machineCode) in validInstructions) {
            assertThat(
                parse(statement).assemble().printAsHex(),
                equalTo(machineCode), { "$statement, once compiled" })
        }
    }

    private val invalidInstructions = listOf(
        "ABC",
        "CLS 200",
        "JP 1000",
        "CALL V5RF",
        "SE R8, EF",
        "SE VAB, EF",
        "LD J, V2",
        "LD V1, (I)"
    )

    @Test
    fun `rejects invalid statements`() {
        invalidInstructions.forEach {
            assertThrows<SyntaxException>(it) { parse(it).assemble() }
        }
    }
}