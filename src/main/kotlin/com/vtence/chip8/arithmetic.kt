package com.vtence.chip8


data class Word(val msb: Byte, val lsb: Byte) {

    override fun toString(): String {
        return String.format("%02X%02X", msb, lsb)
    }

    companion object {
        operator fun invoke(word: Int) = Word((word ushr 8 and 0xFF).toByte(), (word and 0xFF).toByte())
     }
}


fun Int.toWord() = Word(this)

val Int.msb: Byte
    get() = toWord().msb

val Int.lsb: Byte
    get() = toWord().lsb


infix fun Byte.and(other: Byte): Byte = this and other.toInt()

infix fun Byte.and(other: Int): Byte = (this.toInt() and other).toByte()

infix fun Byte.or(other: Byte): Byte = this or other.toInt()

infix fun Byte.or(other: Int): Byte = (this.toInt() or other).toByte()

val Byte.high: Byte
    get() = this and 0xF0

val Byte.low: Byte
    get() = this and 0xF



