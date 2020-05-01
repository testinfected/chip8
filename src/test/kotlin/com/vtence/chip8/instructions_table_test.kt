package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class InstructionsTableTest {

    private val instructions = listOf(
        "CLS" to "000E",
        "RET" to "00EE",
        "SYS 5AF" to "05AF",
        "JP 200" to "1200",
        "CALL A50" to "2A50"
    )

    @Test
    fun `compilation of statement to binary code`() {
        for ((statement, machineCode) in instructions) {
            assertThat(parse(statement).compile { it.toHex() }, equalTo(machineCode), { "$statement, once compiled" })
        }
    }

    private fun <T> Statement.compile(to: (bytes: ByteArray) -> T): T {
        val buffer = ByteArray(2)
        compileTo(ByteBuffer.wrap(buffer))
        return to(buffer)
    }
}