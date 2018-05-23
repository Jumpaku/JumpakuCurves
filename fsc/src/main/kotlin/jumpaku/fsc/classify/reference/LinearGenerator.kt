package jumpaku.fsc.classify.reference


import jumpaku.core.curve.Curve
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3


fun linearConicSectionFromReference(curve: Curve): ConicSection = curve.run {
    val (b, e) = domain
    ConicSection.lineSegment(this(b), this(e))
}

class LinearGenerator(val nSamples: Int = 25) : ReferenceGenerator {
    override fun generate(fsc: Curve, t0: Double, t1: Double): Curve {
        val base = ConicSection.lineSegment(fsc(t0), fsc(t1))
        return ReferenceGenerator.linearPolyline(fsc, t0, t1, base, nSamples)
    }
}