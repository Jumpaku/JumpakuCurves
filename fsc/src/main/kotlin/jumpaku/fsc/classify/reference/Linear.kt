package jumpaku.fsc.classify.reference


import io.vavr.collection.Array
import jumpaku.core.affine.Point
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ArcLengthAdapter
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.divOption

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

    companion object {

        fun ofParams(t0: Double, t1: Double, fsc: BSpline): Linear {
            val arcLengthFsc = fsc.toArcLengthCurve()
            val l = arcLengthFsc.arcLength()
            val l0 = arcLengthFsc.arcLengthUntil(t0)
            val l1 = arcLengthFsc.arcLengthUntil(t1)
            return 1.0.divOption(l1 - l0)
                    .map { Interval(-l0 * it, (l - l0) * it) }
                    .getOrElse { Interval.ZERO_ONE }
                    .let { Linear(ConicSection.lineSegment(fsc(t0), fsc(t1)), it) }
        }

        fun ofBeginEnd(fsc: BSpline): Linear = ofParams(fsc.domain.begin, fsc.domain.end, fsc)

        fun of(fsc: BSpline): Linear = ofBeginEnd(fsc)
    }
}
