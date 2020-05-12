package com.vtence.chip8


sealed class Statement {
    abstract fun writeTo(assembly: Assembly)
}

object BlankStatement : Statement() {
    override fun writeTo(assembly: Assembly) {
    }

    override fun toString() = ""
}

class CommentStatement(private val comment: String) : Statement() {
    override fun writeTo(assembly: Assembly) {
    }

    override fun toString() = "; $comment"
}

class AssemblyStatement(
    val mnemonic: String,
    val operands: List<String> = listOf(),
    val comment: String?
) : Statement() {

    override fun writeTo(assembly: Assembly) {
        INSTRUCTIONS_SET
            .filter { it.mnemonic == mnemonic }
            .filter { it.arity == operands.size }
            .map { runCatching { it.write(assembly, assembly.args(operands)) } }
            .filter { it.isSuccess }
            .find { return }

        throw SyntaxException(toString())
    }

    override fun toString() =
        "$mnemonic${operands.joinToString(prefix = " ").trimEnd()}${comment?.let { " ; $it" }.orEmpty()}"
}


fun Statement.assemble() : Assembly {
    val assembly = Assembly.allocate(2)
    writeTo(assembly)
    return assembly
}


private val BLANK_LINE = Regex("""\s*""")

private val COMMENT_LINE = Regex("""\s*;\s*(?<comment>.*)$""")

private val ASM_LINE = Regex("""(?<mnemonic>\w*)(?:\s+(?<operands>[\s\w$,\[\]]*))?(?:\s+;\s*(?<comment>.*))?""")

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
