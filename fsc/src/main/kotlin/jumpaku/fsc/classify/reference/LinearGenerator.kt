package jumpaku.fsc.classify.reference


import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3


class LinearGenerator(val nSamples: Int = 25) : ReferenceGenerator {

    override fun generate(fsc: Curve, t0: Double, t1: Double): Reference {
        val base = ConicSection.lineSegment(fsc(t0), fsc(t1))
        val (l0, _, l2) = ReferenceGenerator.referenceSubLength(fsc, t0, t1, base)
        val domain = ReferenceGenerator.linearDomain(l0, l2, base)
        return Reference(base, domain)
    }


    fun generateBeginEnd(fsc: Curve): Reference {
        val (t0, t1) = fsc.domain
        val base = ConicSection.lineSegment(fsc(t0), fsc(t1))
        return Reference(base, Interval.ZERO_ONE)
    }
}