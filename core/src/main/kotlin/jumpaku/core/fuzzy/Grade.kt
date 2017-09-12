package jumpaku.core.fuzzy

import com.github.salomonbrys.kotson.double
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import jumpaku.core.json.ToJson
import jumpaku.core.util.clamp


data class Grade(val value: Double) : Comparable<Grade>, ToJson {

    init {
        require(value.isFinite() && value in 0.0..1.0) { "value($value) is out of [0.0, 1.0]." }
    }

    constructor(booleanValue: Boolean): this(if (booleanValue) 1.0 else 0.0)

    constructor(intValue: Int): this(intValue.toDouble())

    override fun compareTo(other: Grade): Int = value.compareTo(other.value)

    infix fun and(g: Grade): Grade = minOf(this, g)

    infix fun or(g: Grade): Grade = maxOf(this, g)

    operator fun not(): Grade = Grade(1.0 - value)

    override fun toString(): String = value.toString()

    override fun toJson(): JsonElement = JsonPrimitive(value)

    companion object {

        val TRUE = Grade(1.0)

        val FALSE = Grade(0.0)

        fun clamped(value: Double): Grade = Grade(clamp(value, 0.0, 1.0))
    }
}

val JsonElement.grade: Grade get() = Grade(double)
