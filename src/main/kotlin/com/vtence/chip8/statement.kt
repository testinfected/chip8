package com.vtence.chip8

import java.io.ByteArrayOutputStream
import java.io.OutputStream


sealed class Statement {
    abstract fun assembleTo(output: OutputStream)

}

object BlankStatement : Statement() {
    override fun assembleTo(output: OutputStream) {
    }

    override fun toString() = ""
}

class CommentStatement(private val comment: String) : Statement() {
    override fun assembleTo(output: OutputStream) {
    }

    override fun toString() = "; $comment"
}

class AssemblyStatement(
    val mnemonic: String,
    val operands: List<String> = listOf(),
    val comment: String?
) : Statement() {

    override fun assembleTo(output: OutputStream) {
        INSTRUCTIONS_SET
            .filter { it.mnemonic == mnemonic }
            .filter { it.arity == operands.size }
            .map { runCatching { output.write(it.numericRepresentation(operands)) } }
            .filter { it.isSuccess }
            .find { return }

        throw SyntaxException(toString())
    }

    override fun toString() =
        "$mnemonic${operands.joinToString(prefix = " ").trimEnd()}${comment?.let { " ; $it" }.orEmpty()}"
}


fun Statement.assemble() = assemble { it }

fun <T> Statement.assemble(format: (bytes: ByteArray) -> T): T {
    val buffer = ByteArrayOutputStream(2)
    assembleTo(buffer)
    return format(buffer.toByteArray())
}


private val BLANK_LINE = Regex("""\s*""")

private val COMMENT_LINE = Regex("""\s*;\s*(?<comment>.*)$""")

private val ASM_LINE = Regex("""(?<mnemonic>\w*)(?:\s+(?<operands>[\s\w$,\[\]]*)\s*[;]*\s*(?<comment>.*))?""")

fun parse(lineOfCode: String): Statement {
    BLANK_LINE.matchEntire(lineOfCode)?.let {
        return BlankStatement
    }

    COMMENT_LINE.matchEntire(lineOfCode)?.let { match ->
        val (comment, _) = match.destructured
        return CommentStatement(comment)
    }

    ASM_LINE.matchEntire(lineOfCode)?.let { match ->
        val (mnemonic, operands, comment) = match.destructured
        return AssemblyStatement(
            mnemonic,
            operands.split(",").map { it.trim() }.filterNot { it.isEmpty() },
            comment.ifEmpty { null }
        )
    }

    throw SyntaxException(lineOfCode)
}


class SyntaxException(msg: String) : IllegalArgumentException(msg)
