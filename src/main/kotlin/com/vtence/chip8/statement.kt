package com.vtence.chip8

import java.nio.ByteBuffer


sealed class Statement(
    open val mnemonic: String? = null,
    open val operands: List<String> = listOf(),
    open val comment: String? = null
) {
    fun compileTo(output: ByteBuffer) {
        INSTRUCTIONS_TABLE
            .asSequence()
            .filter { it.mnemonic == mnemonic }
            .map { runCatching { output.put(it.compile(operands)) } }
            .filter { it.isSuccess }
            .find { return }

        throw SyntaxException(toString())
    }

    override fun toString() =
        "$mnemonic${operands.joinToString(", ", prefix = " ")}${comment?.let { " # $it" }.orEmpty()}"
}

object BlankStatement : Statement()

class CommentStatement(override val comment: String) : Statement(comment = comment)

class AssemblyStatement(
    override val mnemonic: String,
    override val operands: List<String> = listOf(),
    override val comment: String?
) : Statement(mnemonic = mnemonic, comment = comment)

fun Statement.compile() = compile { it }

fun <T> Statement.compile(to: (bytes: ByteArray) -> T): T {
    val buffer = ByteArray(2)
    compileTo(ByteBuffer.wrap(buffer))
    return to(buffer)
}


private val BLANK_LINE = Regex("""\s*""")

private val COMMENT_LINE = Regex("""\s*#\s*(?<comment>.*)$""")

private val ASM_LINE = Regex("""(?<mnemonic>\w*)(?:\s+(?<operands>[\s\w$,]*)\s*[#]*\s*(?<comment>.*))?""")

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
