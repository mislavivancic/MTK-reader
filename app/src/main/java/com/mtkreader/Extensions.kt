package com.mtkreader

/**
 * Compares this value with the specified value for order.
 * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
 * or a positive number if it's greater than other.
 */
fun Byte.compare(other: Int): Int {
    var unsignedValue = this.toInt()
    if (this < 0)
        unsignedValue = -this.toInt()
    return unsignedValue.compareTo(other)
}

fun Byte.toPositiveInt(): Int {
    return toInt() and 0xFF
}