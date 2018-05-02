package jumpaku.fsc.classify.reference


import jumpaku.core.affine.Point
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3


class LinearReferenceGenerator : ReferenceGenerator {
    override fun generate(fsc: FuzzyCurve, t0: Double, t1: Double): ReferenceCurve {
        val base = ConicSection.lineSegment(fsc(t0), fsc(t1))
        val (l0, l1, l2) = ReferenceGenerator.referenceSubLength(fsc, t0, t1, base)
        val changeLinearParam = ReferenceGenerator.changeLinearParam(l0, l1)
        fun eval(t: Double): Point = ReferenceGenerator.conicSectionWithoutDomain(base, changeLinearParam(t))
        return object : ReferenceCurve() {
            override val domain: Interval = Interval(0.0, l0 + l1 + l2)
            override val conicSection: ConicSection by lazy {
                ConicSection.lineSegment(eval(domain.begin), eval(domain.end))
            }
            override fun evaluate(t: Double): Point = eval(t)
        }
    }
}