package org.jumpaku.curve

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import org.jumpaku.json.prettyGson

/**
 * Created by jumpaku on 2017/05/22.
 */
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

    override fun toString(): String = KnotJson.toJson(this)

    companion object {

        fun clampedUniformKnots(degree: Int, nControlPoints: Int): Array<Knot> {
            return clampedUniformKnots(0.0, (nControlPoints - degree).toDouble(), degree, nControlPoints)
        }

        fun clampedUniformKnots(domain: Interval, degree: Int, knotSize: Int): Array<Knot> {
            return clampedUniformKnots(domain.begin, domain.end, degree, knotSize)
        }

        fun clampedUniformKnots(begin: Double, end: Double, degree: Int, knotSize: Int): Array<Knot> {
            val l = knotSize - 2 * degree - 1

            return Stream.of(Knot(begin, degree + 1))
                    .appendAll(Stream.range(1, l).map { Knot((1.0 - it) * begin / l + it * end / l, 1) })
                    .appendAll(Stream.of(Knot(end, degree + 1)))
                    .toArray()
        }
    }
}

data class KnotJson(val value: Double, val multiplicity: Int){

    companion object{

        fun toJson(knot: Knot): String = prettyGson.toJson(knot.run { KnotJson(value, multiplicity) })

        fun fromJson(json: String): Option<Knot> {
            return try {
                Option(prettyGson.fromJson<KnotJson>(json).run { Knot(value, multiplicity) })
            }
            catch (e: Exception){
                None()
            }
        }
    }
}
