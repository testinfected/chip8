package com.vtence.chip8


typealias Word = Pair<Byte, Byte>

val Word.msb: Byte
        get() = this.first

val Word.lsb: Byte
        get() = this.second


fun Int.toWord() = Word((this ushr 8 and 0xFF).toByte(), (this and 0xFF).toByte())

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



