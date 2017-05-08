package org.jumpaku.fuzzy


class Grade(val value: Double) : Comparable<Grade> {

    init {
        if (!java.lang.Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw IllegalArgumentException("value $value is out of [0.0, 1.0].")
        }
    }

    override fun toString(): String = value.toString()

    override fun compareTo(g: Grade): Int = compare(this, g)

    fun and(g: Grade): Grade = and(this, g)

    fun or(g: Grade): Grade = or(this, g)

    fun not(): Grade = not(this)

    companion object {

        val TRUE = Grade(1.0)

        val FALSE = Grade(0.0)

        fun and(a: Grade, b: Grade): Grade = minOf(a, b)

        fun or(a: Grade, b: Grade): Grade = maxOf(a, b)

        fun not(g: Grade): Grade = Grade(1.0 - g.value)

        fun compare(a: Grade, b: Grade): Int = java.lang.Double.compare(a.value, b.value)

    }
}
