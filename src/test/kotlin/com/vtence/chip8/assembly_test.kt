package com.vtence.chip8

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.junit.jupiter.api.Test

class AssemblyTest {

    @Test
    fun `an empty program`() {
        val program = Program.source("""""")

        val machineCode = program.assemble()

        assertThat(machineCode, hasContent(""))
    }

    @Test
    fun `a very dumb program`() {
        val program = Program.source(
            """
            JP 200
        """.trimIndent()
        )

        val machineCode = program.assemble()

        assertThat(machineCode, hasContent("1200"))
    }

    @Test
    fun `an extract of the maze program`() {
        val program = Program.source(
            """
            ; Lets's say LOOP starts at 204h
            ; Left line is at 21Eh
            ; Right line is at 226h
            
            LD  I, 21E      ; We draw a left line by default, as the random number
                            ; is 0 or 1. If we suppose that it will be 1, we keep
                            ; drawing the left line. If it is 0, we change register
                            ; I to draw a right line.
         
            RND V2, 1       ; Load in V2 a 0...1 random number
         
            SE  V2, 1       ; It is 1 ? If yes, I still refers to the left line
                            ; bitmap.
         
            LD  I, 226      ; If not, we change I to make it refer the right line
                            ; bitmap.
         
            DRW V0, V1, 4   ; And we draw the bitmap at V0, V1.
         
            ADD V0, 4       ; The next bitmap is 4 pixels right. So we update
                            ; V0 to do so.
         
            SE  V0, 64      ; If V0==64, we finished drawing a complete line, so we
                            ; skip the jump to LOOP, as we have to update V1 too.
         
            JP  204         ; We did not draw a complete line ? So we continue !
         
            LD  V0, 0       ; The first bitmap of each line is located 0, V1.
         
            ADD V1, 4       ; We update V1. The next line is located 4 pixels doan.
         
            SE  V1, 32      ; Have we drawn all the lines ? If yes, V1==32.
            JP  204         ; No ? So we continue !        
        """.trimIndent()
        )

        val machineCode = program.assemble()

        assertThat(
            machineCode, hasContent(
                "A21E",
                "C201",
                "3201",
                "A226",
                "D014",
                "7004",
                "3064",
                "1204",
                "6000",
                "7104",
                "3132",
                "1204"
            )
        )
    }

    private fun hasContent(vararg words: String): Matcher<ByteArray> =
        has("content", { it.toHex() }, equalTo(words.joinToString("").toLowerCase()))
}