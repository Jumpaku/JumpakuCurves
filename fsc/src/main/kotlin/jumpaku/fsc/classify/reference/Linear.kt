package jumpaku.fsc.classify.reference


import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3

class Linear(polyline: Polyline) : Reference(polyline) {
    override val conicSection: ConicSection by lazy {
        evaluateAll(3).let { (s0, sf, s2) -> ConicSection(s0, sf, s2, 1.0) }
    }
}

class LinearGenerator(val nSamples: Int = 25) : ReferenceGenerator {
    override fun generate(fsc: FuzzyCurve, t0: Double, t1: Double): Reference {
        val base = ConicSection.lineSegment(fsc(t0), fsc(t1))
        return Linear(ReferenceGenerator.linearPolyline(fsc, t0, t1, base, nSamples))
    }
}