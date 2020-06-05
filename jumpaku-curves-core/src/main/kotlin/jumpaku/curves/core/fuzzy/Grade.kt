package jumpaku.curves.core.fuzzy



data class Grade(val value: Double) : Comparable<Grade> {

    init {
        require(value.isFinite() && value in 0.0..1.0) { "value($value) is out of [0.0, 1.0]." }
    }

    override fun compareTo(other: Grade): Int = value.compareTo(other.value)

    infix fun and(g: Grade): Grade = minOf(this, g)

    infix fun or(g: Grade): Grade = maxOf(this, g)

    operator fun not(): Grade = Grade((1.0 - value).coerceIn(0.0, 1.0))

    fun toBoolean(greaterThan: Double = 0.5, orEqual: Boolean = true): Boolean =
            if (orEqual) value >= greaterThan else value > greaterThan

    companion object {

        val TRUE = Grade(1.0)

        val FALSE = Grade(0.0)

        operator fun invoke(booleanValue: Boolean) = if (booleanValue) TRUE else FALSE

        fun clamped(value: Double): Grade = Grade(value.coerceIn(0.0, 1.0))

    }
}

