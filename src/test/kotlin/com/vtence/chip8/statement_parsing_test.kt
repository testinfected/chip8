package com.vtence.chip8

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import org.junit.jupiter.api.Test

class StatementParsing {

    @Test
    fun `blank statement`() {
        val statement = parse("")

        assertThat(statement, isA<BlankStatement>())
    }

    @Test
    fun `comment statement`() {
        val statement = parse("# A comment line that contains nothing")

        assertThat(statement, isA<CommentStatement>())
        assertThat(statement.comment, equalTo("A comment line that contains nothing"))
    }

    @Test
    fun `simplest assembly statement, using only a mnemonic`() {
        val statement = parse("CLS")

        assertThat(statement, isA<AssemblyStatement>())
        assertThat(statement.mnemonic, equalTo("CLS"))
    }

    @Test
    fun `assembly statement followed by a comment`() {
        val statement = parse("CLS # Clear screen")

        assertThat(statement, isA<AssemblyStatement>())
        assertThat(statement.mnemonic, equalTo("CLS"))
        assertThat(statement.comment, equalTo("Clear screen"))
    }

    @Test
    fun `assembly statement with a single operand`() {
        val statement = parse("CALL 200")

        assertThat(statement, isA<AssemblyStatement>())
        assertThat(statement.mnemonic, equalTo("CALL"))
        assertThat(statement.operands, equalTo(listOf("200")))
    }

    @Test
    fun `assembly statement with two operands`() {
        val statement = parse("ADD V1, 10")

        assertThat(statement, isA<AssemblyStatement>())
        assertThat(statement.operands, equalTo(listOf("V1", "10")))
    }

    @Test
    fun `assembly statement with operands, followed by a comment`() {
        val statement = parse("ADD V1, 10 # Add 10h to register V1")

        assertThat(statement, isA<AssemblyStatement>())
        assertThat(statement.mnemonic, equalTo("ADD"))
        assertThat(statement.operands, equalTo(listOf("V1", "10")))
        assertThat(statement.comment, equalTo("Add 10h to register V1"))
    }
}