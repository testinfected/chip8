package com.vtence.chip8

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.junit.jupiter.api.Test

class AssemblerTest {

    @Test
    fun `an empty program`() {
        val program = Program.source("""""")

        val machineCode = Assembler.assemble(program)

        assertThat(machineCode, hasContent(""))
    }

    @Test
    fun `a very dumb program`() {
        val program = Program.source(
            """
            JP 200
        """.trimIndent()
        )

        val machineCode = Assembler.assemble(program)

        assertThat(machineCode, hasContent("1200"))
    }

    @Test
    fun `an extract of the maze program`() {
        val program = Program.source(
            """
                LD  V0, 0
                LD  V1, 0
            
            LOOP:
                LD  I, LEFT     ; We draw a left line by default, as the random number
                                ; is 0 or 1. If we suppose that it will be 1, we keep
                                ; drawing the left line. If it is 0, we change register
                                ; I to draw a right line.
             
                RND V2, 1       ; Load in V2 a 0...1 random number
             
                SE  V2, 1       ; It is 1? If yes, I still refers to the left line
                                ; bitmap.
             
                LD  I, RIGHT    ; If not, we change I to make it refer the right line
                                ; bitmap.
             
                DRW V0, V1, 4   ; And we draw the bitmap at V0, V1.
             
                ADD V0, 4       ; The next bitmap is 4 pixels right. So we update
                                ; V0 to do so.
             
                SE  V0, 64      ; If V0==64, we finished drawing a complete line, so we
                                ; skip the jump to LOOP, as we have to update V1 too.
             
                JP  LOOP        ; We did not draw a complete line? So we continue!
             
                LD  V0, 0       ; The first bitmap of each line is located 0, V1.
             
                ADD V1, 4       ; We update V1. The next line is located 4 pixels doan.
             
                SE  V1, 32      ; Have we drawn all the lines? If yes, V1==32.
                JP  LOOP        ; No? So we continue! 
                   
            LEFT:               ; 4*4 bitmap of the left line
                BYTE 1.......
                BYTE .1......
                BYTE ..1.....
                BYTE ...1....
 
            RIGHT:                ; 4*4 bitmap of the right line
                BYTE ..1.....
                BYTE .1......
                BYTE 1.......
                BYTE ...1....
        """.trimIndent()
        )

        val machineCode = Assembler.assemble(program)

        assertThat(
            machineCode, hasContent(
                "6000",
                "6100",
                "A21C",
                "C201",
                "3201",
                "A220",
                "D014",
                "7004",
                "3064",
                "1204",
                "6000",
                "7104",
                "3132",
                "1204",
                "8040",
                "2010",
                "2040",
                "8010"
            )
        )
    }

    private fun hasContent(vararg words: String): Matcher<Assembly> =
        has("content", { it.printAsHex(false) }, equalTo(words.joinToString("").toLowerCase()))
}