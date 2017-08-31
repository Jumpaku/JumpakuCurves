package org.jumpaku.fsc.identify.reference


import io.vavr.collection.Array
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.IntervalJson
import org.jumpaku.core.curve.arclength.ArcLengthAdapter
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.curve.rationalbezier.ConicSectionJson
import org.jumpaku.core.fuzzy.Grade
import org.jumpaku.core.json.prettyGson

class Linear(val conicSection: ConicSection, val domain: Interval) : Reference {

    val begin: Point = conicSection.begin

    val end: Point = conicSection.end

    val reference: FuzzyCurve = object : FuzzyCurve {

        override val domain: Interval = this@Linear.domain

        override fun toArcLengthCurve(): ArcLengthAdapter = ArcLengthAdapter(
                this, Array.of(domain.begin, 0.0, 1.0, domain.end).filter { it in domain })

        override fun evaluate(t: Double): Point {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return begin.divide(t, end)
        }
    }

    override fun isValidFor(fsc: BSpline): Grade = reference.isPossible(fsc)

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): LinearJson = LinearJson(this)

    companion object {

        fun ofParams(t0: Double, t1: Double, fsc: BSpline): Linear {
            val arcLengthFsc = fsc.toArcLengthCurve()
            val l = arcLengthFsc.arcLength()
            val l0 = arcLengthFsc.arcLengthUntil(t0)
            val l1 = arcLengthFsc.arcLengthUntil(t1)
            return Linear(ConicSection(fsc(t0), fsc(t0).middle(fsc(t1)), fsc(t1), 1.0),
                    Interval(-l0 / (l1 - l0), (l - l0) / (l1 - l0)))
        }

        fun ofBeginEnd(fsc: BSpline): Linear {
            return ofParams(fsc.domain.begin, fsc.domain.end, fsc)
        }

        fun of(fsc: BSpline): Linear {
            return ofBeginEnd(fsc)
        }
    }
}

data class LinearJson(val conicSection: ConicSectionJson, val domain: IntervalJson){

    constructor(linear: Linear) : this(linear.conicSection.json(), linear.domain.json())

    fun linear(): Linear = Linear(conicSection.conicSection(), domain.interval())
}
