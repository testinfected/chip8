package com.vtence.chip8

import java.lang.IllegalArgumentException

sealed class Statement(
    open val mnemonic: String? = null,
    open val operands: List<String> = listOf(),
    open val comment: String? = null
)

object BlankStatement : Statement()

data class CommentStatement(override val comment: String?) : Statement(comment = comment)

data class AssemblyStatement(override val mnemonic: String?,
                             override val operands: List<String> = listOf(),
                             override val comment: String?) :
    Statement(mnemonic = mnemonic, comment = comment)


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
        return AssemblyStatement(mnemonic, operands = operands.split(",").map { it.trim() }, comment = comment)
    }

    throw IllegalArgumentException(lineOfCode)
}