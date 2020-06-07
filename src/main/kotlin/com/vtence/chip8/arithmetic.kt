package com.vtence.chip8


data class Word(val msb: Byte, val lsb: Byte) {

    fun toHex(upperCase: Boolean = false) = msb.toHex(upperCase) + lsb.toHex(upperCase)

    companion object {
        operator fun invoke(word: Int) = Word((word ushr 8 and 0xFF).toByte(), (word and 0xFF).toByte())
    }
}

fun Int.toWord() = Word(this)


infix fun Byte.and(other: Byte): Byte = this and other.toInt()

infix fun Byte.and(other: Int): Byte = (this.toInt() and other).toByte()

infix fun Byte.or(other: Byte): Byte = this or other.toInt()

infix fun Byte.or(other: Int): Byte = (this.toInt() or other).toByte()

val Byte.high: Byte
    get() = this and 0xF0

val Byte.low: Byte
    get() = this and 0xF

fun Byte.toHex(upperCase: Boolean = false) = String.format(if (upperCase) "%02X" else "%02x", this)


fun String.toWord() = toInt(radix = 16).toWord()
