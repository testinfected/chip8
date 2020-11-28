package com.vtence.chip8

import java.io.Reader
import java.io.StringReader


class Program(private val statements: List<Statement>) : Sequence<Statement> {

    val lines: List<String>
        get() = map { it.toString() }.toList()

    override fun iterator() = statements.iterator()

    companion object {
        operator fun invoke(vararg statements: Statement) = Program(statements.toList())

        fun read(source: Reader) = Program(source.readLines().map { parse(it) })

        fun fromSource(assemblyCode: String): Program = read(StringReader(assemblyCode))
    }
}


sealed class Statement

object BlankLine : Statement() {
    override fun toString() = ""
}

class Comment(private val comment: String) : Statement() {
    override fun toString() = "; $comment"
}

class LabelDefinition(val label: String, val comment: String?) : Statement() {
    override fun toString() = "$label: ${comment?.let { "; $it" }.orEmpty()}".trimEnd()
}

class AssemblyCode(
    val mnemonic: String,
    val operands: List<String> = listOf(),
    val comment: String? = null
) : Statement() {

    override fun toString() =
        "$mnemonic${operands.joinToString(prefix = " ").trimEnd()}${comment?.let { " ; $it" }.orEmpty()}"
}


private val BLANK_LINE = Regex("""\s*""")

private val COMMENT_LINE = Regex("""\s*;\s*(?<comment>.*)""")

private val LABEL_DEFINITION = Regex("""\s*(?<label>[^:]+):(?:$COMMENT_LINE)?""")

private val ASM_LINE = Regex("""\s*(?<mnemonic>\w*)(?:\s+(?<operands>[\s\w.,\[\]]*))?(?:\s+$COMMENT_LINE)?""")

fun parse(lineOfCode: String): Statement {
    BLANK_LINE.matchEntire(lineOfCode)?.let {
        return BlankLine
    }

    COMMENT_LINE.matchEntire(lineOfCode)?.let { match ->
        val (comment, _) = match.destructured
        return Comment(comment)
    }

    LABEL_DEFINITION.matchEntire(lineOfCode)?.let { match ->
        val (label, comment) = match.destructured
        return LabelDefinition(label, comment.ifEmpty { null })
    }

    ASM_LINE.matchEntire(lineOfCode)?.let { match ->
        val (mnemonic, operands, comment) = match.destructured
        return AssemblyCode(
            mnemonic,
            operands.split(",").map { it.trim() }.filterNot { it.isEmpty() },
            comment.ifEmpty { null }
        )
    }

    throw SyntaxException(lineOfCode)
}


class SyntaxException(msg: String) : IllegalArgumentException(msg)
