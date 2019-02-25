package jumpaku.fsc.snap.conicsection

import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import org.apache.commons.math3.util.FastMath


class ConjugateCombinator : FeaturePointsCombinator {

    enum class FeaturePosition {
        B0, B2, D0, D1, D2, D3, E0, E1, E2, E3
    }

    private operator fun ConicSection.get(featurePosition: FeaturePosition): Point {
        val t = ((1 - FastMath.sqrt((1 + weight) / (1 - weight))) / 2).coerceIn(0.0, 0.5)
        val o = center().orThrow().toCrisp()
        return when (featurePosition) {
            FeaturePosition.B0 -> begin
            FeaturePosition.B2 -> end
            FeaturePosition.D0 -> evaluate(t)
            FeaturePosition.D1 -> far
            FeaturePosition.D2 -> evaluate(1 - t)
            FeaturePosition.D3 -> complement().far
            FeaturePosition.E0 -> o.lerp(FastMath.sqrt(2.0), get(FeaturePosition.D0))
            FeaturePosition.E1 -> o.lerp(FastMath.sqrt(2.0), get(FeaturePosition.D1))
            FeaturePosition.E2 -> o.lerp(FastMath.sqrt(2.0), get(FeaturePosition.D2))
            FeaturePosition.E3 -> o.lerp(FastMath.sqrt(2.0), get(FeaturePosition.D3))
        }
    }

    override fun linearCombinations(conicSection: ConicSection, isOpen: Boolean): List<LinearFeaturePoints> = when{
        isOpen -> listOf(LinearFeaturePoints(conicSection.begin, conicSection.end))
        else -> listOf(LinearFeaturePoints(conicSection.far, conicSection.far))
    }

    override fun circularCombinations(conicSection: ConicSection, isOpen: Boolean): List<CircularFeaturePoints> = when {
        conicSection.weight > 0.0 -> listOf(
                Triple(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                Triple(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.E1)
        )
        isOpen -> listOf(
                Triple(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                Triple(FeaturePosition.D0, FeaturePosition.D2, FeaturePosition.D1),
                Triple(FeaturePosition.E0, FeaturePosition.E2, FeaturePosition.D1)
         )
        else -> listOf(
                Triple(FeaturePosition.D0, FeaturePosition.D2, FeaturePosition.D1),
                Triple(FeaturePosition.D0, FeaturePosition.D2, FeaturePosition.D3),
                Triple(FeaturePosition.D1, FeaturePosition.D3, FeaturePosition.D0),
                Triple(FeaturePosition.D1, FeaturePosition.D3, FeaturePosition.D2),
                Triple(FeaturePosition.E0, FeaturePosition.E2, FeaturePosition.D1),
                Triple(FeaturePosition.E0, FeaturePosition.E2, FeaturePosition.D3),
                Triple(FeaturePosition.E1, FeaturePosition.E3, FeaturePosition.D0),
                Triple(FeaturePosition.E1, FeaturePosition.E3, FeaturePosition.D2)
        )
    } .map { (p0, p1, pn) -> CircularFeaturePoints(conicSection[p0], conicSection[p1], conicSection[pn])}

    override fun ellipticCombinations(conicSection: ConicSection, isOpen: Boolean): List<EllipticFeaturePoints> = when {
        conicSection.weight > 0.0 -> listOf(
                Triple(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                Triple(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.E1)
        )
        isOpen -> listOf(
                Triple(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                Triple(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.E1),
                Triple(FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2),
                Triple(FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2)
        )
        else -> listOf(
                Triple(FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2),
                Triple(FeaturePosition.D1, FeaturePosition.D2, FeaturePosition.D3),
                Triple(FeaturePosition.D2, FeaturePosition.D3, FeaturePosition.D0),
                Triple(FeaturePosition.D3, FeaturePosition.D0, FeaturePosition.D1),
                Triple(FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2),
                Triple(FeaturePosition.E1, FeaturePosition.E2, FeaturePosition.E3),
                Triple(FeaturePosition.E2, FeaturePosition.E3, FeaturePosition.E0),
                Triple(FeaturePosition.E3, FeaturePosition.E0, FeaturePosition.E1)
        )
    }.map { (f0, f1, f2) -> EllipticFeaturePoints(conicSection[f0], conicSection[f1], conicSection[f2]) }
}
