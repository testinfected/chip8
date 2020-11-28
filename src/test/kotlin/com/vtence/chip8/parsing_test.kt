package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.isEmptyString
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParsingTest {

    @Test
    fun `blank statement`() {
        val statement = parse("")

        assertThat(statement, isA<BlankLine>())
        assertThat(statement.toString(), isEmptyString)
    }

    @Test
    fun `comment statement`() {
        val statement = parse("; a comment line that contains nothing")

        assertThat(statement, isA<Comment>())
        assertThat(statement.toString(), equalTo("; a comment line that contains nothing"))
    }

    @Test
    fun `simplest assembly statement, using only a mnemonic`() {
        when (val statement = parse("CLS")) {
            is AssemblyCode -> {
                assertThat(statement.mnemonic, equalTo("CLS"))
                assertThat(statement.toString(), equalTo("CLS"))
            }
            else -> invalidTypeOf(statement)
        }
    }

    @Test
    fun `assembly statement followed by a comment`() {
        when (val statement = parse("CLS ; clear screen")) {
            is AssemblyCode -> {
                assertThat(statement.mnemonic, equalTo("CLS"))
                assertThat(statement.comment, equalTo("clear screen"))
                assertThat(statement.toString(), equalTo("CLS ; clear screen"))
            }
            else -> invalidTypeOf(statement)
        }
    }

    @Test
    fun `assembly statement with a single operand`() {
        when (val statement = parse("CALL 200")) {
            is AssemblyCode -> {
                assertThat(statement.mnemonic, equalTo("CALL"))
                assertThat(statement.operands, equalTo(listOf("200")))
                assertThat(statement.toString(), equalTo("CALL 200"))
            }
            else -> invalidTypeOf(statement)
        }
    }

    @Test
    fun `assembly statement with two operands`() {
        when (val statement = parse("ADD V1, 10")) {
            is AssemblyCode -> {
                assertThat(statement.mnemonic, equalTo("ADD"))
                assertThat(statement.operands, equalTo(listOf("V1", "10")))
                assertThat(statement.toString(), equalTo("ADD V1, 10"))
            }
            else -> invalidTypeOf(statement)
        }
    }

    @Test
    fun `assembly statement with operands, followed by a comment`() {
        when (val statement = parse("ADD V1, 10 ; add 10h to register V1")) {
            is AssemblyCode -> {
                assertThat(statement.mnemonic, equalTo("ADD"))
                assertThat(statement.operands, equalTo(listOf("V1", "10")))
                assertThat(statement.comment, equalTo("add 10h to register V1"))
                assertThat(statement.toString(), equalTo("ADD V1, 10 ; add 10h to register V1"))
            }
            else -> invalidTypeOf(statement)
        }
    }

    @Test
    fun `label definition`() {
        when (val statement = parse("LOOP:")) {
            is LabelDefinition -> {
                assertThat(statement.label, equalTo("LOOP"))
                assertThat(statement.toString(), equalTo("LOOP:"))
            }
            else -> invalidTypeOf(statement)
        }
    }

    @Test
    fun `label definition with a comment`() {
        when (val statement = parse("LOOP: ; label definition")) {
            is LabelDefinition -> {
                assertThat(statement.label, equalTo("LOOP"))
                assertThat(statement.toString(), equalTo("LOOP: ; label definition"))
            }
            else -> invalidTypeOf(statement)
        }
    }

    @Test
    fun `invalid statement`() {
        assertThrows<SyntaxException> { parse("invalid statement !@#$!@#") }
    }

    private fun invalidTypeOf(statement: Statement): Nothing =
        fail("was a ${statement.javaClass.kotlin.qualifiedName}")
}