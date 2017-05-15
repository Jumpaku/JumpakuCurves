package org.jumpaku.curve

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParseException
import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.jumpaku.json.prettyGson


class Interval(val begin: Double, val end: Double) {

    init {
        if (begin > end){
            throw IllegalArgumentException("begin($begin) > end($end)")
        }
    }

    fun sample(n: Int): Array<Double> {
        return when{
            n == 1 && Precision.equals(begin, end, 1.0e-10) -> Array(begin)
            n >= 2 -> Stream.range(0, n)
                    .map { (n - 1 - it) * begin / (n - 1) + it * end / (n - 1) }
                    .toArray()
            else -> throw IllegalArgumentException("n($n) is too small")
        }
    }

    fun sample(delta: Double): Array<Double> = sample(FastMath.ceil((end - begin) / delta).toInt())

    operator fun contains(t: Double): Boolean = t in begin..end

    operator fun contains(i: Interval): Boolean = i.begin in begin..i.end && i.end in i.begin..end

    override fun toString(): String = Interval.toJson(this)

    companion object {

        val ZERO_ONE = Interval(0.0, 1.0)

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