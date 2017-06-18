package org.jumpaku.core.fuzzy


data class Grade(val value: Double) : Comparable<Grade> {

    init {
        if (!java.lang.Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw IllegalArgumentException("value($value) is out of [0.0, 1.0].")
        }
    }

    constructor(booleanValue: Boolean): this(if (booleanValue) 1.0 else 0.0)

    constructor(intValue: Int): this(intValue.toDouble())

    override fun toString(): String = value.toString()

    override fun compareTo(other: Grade): Int = value.compareTo(other.value)

    infix fun and(g: Grade): Grade = minOf(this, g)

    infix fun or(g: Grade): Grade = maxOf(this, g)

    operator fun not(): Grade = Grade(1.0 - value)

    companion object {

        val TRUE = Grade(1.0)

        val FALSE = Grade(0.0)

        fun clamp(value: Double): Double = minOf(1.0, maxOf(0.0, value))
    }
}
