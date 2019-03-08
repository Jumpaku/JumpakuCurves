package jumpaku.curves.core.fuzzy

import com.github.salomonbrys.kotson.double
import com.google.gson.JsonPrimitive
import jumpaku.curves.core.json.ToJson


data class Grade(val value: Double) : Comparable<Grade>, ToJson {

    init {
        require(value.isFinite() && value in 0.0..1.0) { "value($value) is out of [0.0, 1.0]." }
    }

    override fun compareTo(other: Grade): Int = value.compareTo(other.value)

    infix fun and(g: Grade): Grade = minOf(this, g)

    infix fun or(g: Grade): Grade = maxOf(this, g)

    operator fun not(): Grade = Grade((1.0 - value).coerceIn(0.0, 1.0))

    fun toBoolean(greaterThan: Double = 0.5, orEqual: Boolean = true): Boolean =
            if (orEqual) value >= greaterThan else value > greaterThan

    override fun toString(): String = value.toString()

    override fun toJson(): JsonPrimitive = JsonPrimitive(value)

    companion object {

        val TRUE = Grade(1.0)

        val FALSE = Grade(0.0)

        operator fun invoke(booleanValue: Boolean) = if (booleanValue) Grade.TRUE else Grade.FALSE

        fun clamped(value: Double): Grade = Grade(value.coerceIn(0.0, 1.0))

        fun fromJson(json: JsonPrimitive): Grade = Grade(json.double)
    }
}

