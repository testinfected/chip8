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

class LabelDefinition(val label: String, val comment: String?) : Statement() {
    override fun writeTo(assembly: Assembly) {
        assembly.mark(label)
    }

    override fun toString() = "$label: ${comment?.let { "; $it" }.orEmpty()}".trimEnd()
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
    val assembly = Assembly.allocate(2, base = 0)
    writeTo(assembly)
    return assembly
}


private val BLANK_LINE = Regex("""\s*""")

private val COMMENT_LINE = Regex("""\s*;\s*(?<comment>.*)""")

private val LABEL_DEFINITION = Regex("""\s*(?<label>[^:]+):(?:$COMMENT_LINE)?""")

private val ASM_LINE = Regex("""\s*(?<mnemonic>\w*)(?:\s+(?<operands>[\s\w.,\[\]]*))?(?:\s+$COMMENT_LINE)?""")

fun parse(lineOfCode: String): Statement {
    BLANK_LINE.matchEntire(lineOfCode)?.let {
        return BlankStatement
    }

    COMMENT_LINE.matchEntire(lineOfCode)?.let { match ->
        val (comment, _) = match.destructured
        return CommentStatement(comment)
    }

    LABEL_DEFINITION.matchEntire(lineOfCode)?.let { match ->
        val (label, comment) = match.destructured
        return LabelDefinition(label, comment.ifEmpty { null })
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
