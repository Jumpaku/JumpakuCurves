package org.jumpaku.core.curve

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import org.jumpaku.core.affine.divide
import org.jumpaku.core.json.prettyGson


data class Knot(val value: Double, val multiplicity: Int = 1) {

    init {
        if (multiplicity < 0) {
            throw IllegalArgumentException("negative multiplicity($multiplicity)")
        }
    }

    fun toArray(): Array<Double> = Stream.fill(multiplicity, { value }).toArray()

    fun reduceMultiplicity(): Knot = reduceMultiplicity(1)

    fun reduceMultiplicity(m: Int): Knot = Knot(value, multiplicity - m)

    fun elevateMultiplicity(): Knot = elevateMultiplicity(1)

    fun elevateMultiplicity(m: Int): Knot = Knot(value, multiplicity + m)

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): KnotJson = KnotJson(this)

    companion object {

        fun clampedUniformKnots(degree: Int, knotSize: Int): Array<Knot> {
            return clampedUniformKnots(0.0, knotSize - 1.0 - 2 * degree, degree, knotSize)
        }

        fun clampedUniformKnots(domain: Interval, degree: Int, knotSize: Int): Array<Knot> {
            return clampedUniformKnots(domain.begin, domain.end, degree, knotSize)
        }

        fun clampedUniformKnots(begin: Double, end: Double, degree: Int, knotSize: Int): Array<Knot> {
            val l = knotSize - 2 * degree - 1
            return Stream(Knot(begin, degree + 1))
                    .appendAll(Stream.range(1, l).map { Knot(begin.divide(it/l.toDouble(), end))})
                    .append(Knot(end, degree + 1))
                    .toArray()
        }
    }
}

data class KnotJson(private val value: Double, private val multiplicity: Int){

    constructor(knot: Knot) : this(knot.value, knot.multiplicity)

    fun  knot(): Knot = Knot(value, multiplicity)
}
