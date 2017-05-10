package org.jumpaku.curve

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParseException
import org.apache.commons.math3.util.FastMath
import org.jumpaku.json.prettyGson

val ZERO_ONE = Interval(0.0, 1.0)

class Interval(val begin: Double, val end: Double) {

    fun sample(n: Int): List<Double> {
        if (n < 2) {
            throw IllegalArgumentException("n=$n is too small, n >= 2")
        }

        return (0..n-1)
                .map { (n - 1 - it) * begin / (n - 1) + it * end / (n - 1) }
                .apply { listOf(toTypedArray()) }
    }

    fun sample(delta: Double): List<Double> = sample(FastMath.ceil((end - begin) / delta).toInt())

    operator fun contains(t: Double): Boolean = t in begin..end

    operator fun contains(i: Interval): Boolean = i.begin in begin..i.end && i.end in i.begin..end

    override fun toString(): String = Interval.toJson(this)

    companion object {

        data class JsonInterval(val begin: Double, val end: Double)

        fun toJson(i: Interval): String = prettyGson.toJson(JsonInterval(i.begin, i.end))

        fun fromJson(json: String): Interval? {
            return try {
                val v = prettyGson.fromJson<JsonInterval>(json)
                Interval(v.begin, v.end)
            } catch(e: Exception) {
                when (e) {
                    is IllegalArgumentException, is JsonParseException -> null
                    else -> throw e
                }
            }
        }
    }
}