package org.jumpaku.core.curve

import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.divide
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.clamp


data class Interval(val begin: Double, val end: Double) {

    val span: Double = end - begin

    init {
        require(begin <= end){ "begin($begin) > end($end)" }
    }

    fun sample(samplesCount: Int): Array<Double> {
        require(samplesCount >= 2) { "samplesCount($samplesCount) < 2" }
        return Stream.range(0, samplesCount)
                .map { clamp(begin.divide(it/(samplesCount - 1.0), end), this)  }
                .toArray()
    }

    fun sample(delta: Double): Array<Double> = sample(FastMath.ceil((end - begin) / delta).toInt() + 1)

    operator fun contains(t: Double): Boolean = t in begin..end

    operator fun contains(i: Interval): Boolean = i.begin in begin..i.end && i.end in i.begin..end

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): IntervalJson = IntervalJson(this)

    companion object {
        val ZERO_ONE = Interval(0.0, 1.0)
    }
}


data class IntervalJson(private val begin: Double, private val end: Double) {

    constructor(interval: Interval) : this(interval.begin, interval.end)

    fun interval(): Interval = Interval(begin, end)
}