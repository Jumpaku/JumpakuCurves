package org.jumpaku.core.curve

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.divide
import org.jumpaku.core.json.prettyGson


data class Interval(val begin: Double, val end: Double) {

    val span: Double = end - begin

    init {
        require(begin <= end){ "begin($begin) > end($end)" }
    }

    fun sample(nSamples: Int): Array<Double> {
        return when{
            nSamples == 1 && Precision.equals(begin, end, 1.0e-10) -> Array(begin)
            nSamples >= 2 -> Stream.range(0, nSamples)
                    .map { begin.divide(it/(nSamples - 1.0), end)  }
                    .toArray()
            else -> throw IllegalArgumentException("n($nSamples) is too small")
        }
    }

    fun sample(delta: Double): Array<Double> = sample(FastMath.ceil((end - begin) / delta).toInt() + 1)

    operator fun contains(t: Double): Boolean = t in begin..end

    operator fun contains(i: Interval): Boolean = i.begin in begin..i.end && i.end in i.begin..end

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): IntervalJson = IntervalJson(this)

    companion object{
        val ZERO_ONE = Interval(0.0, 1.0)
    }
}


data class IntervalJson(private val begin: Double, private val end: Double) {

    constructor(interval: Interval) : this(interval.begin, interval.end)

    fun interval(): Interval = Interval(begin, end)
}