package com.vtence.chip8


fun String.toHex(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun ByteArray.toHex(upper: Boolean = false) = joinToString("") {
    "%02x".format(it).let { hex -> if (upper) hex.toUpperCase() else hex }
}
