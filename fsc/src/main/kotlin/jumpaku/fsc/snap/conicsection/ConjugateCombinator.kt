package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple3
import io.vavr.collection.Stream
import jumpaku.core.geom.Point
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.util.FastMath


class ConjugateCombinator : FeaturePointsCombinator {

    enum class FeaturePosition {
        B0, B2, O, D0, D1, D2, D3, E0, E1, E2, E3
    }

    private operator fun ConicSection.get(featurePosition: FeaturePosition): Point {
        val t = ((1 - FastMath.sqrt((1 + weight) / (1 - weight))) / 2).coerceIn(0.0, 0.5)
        return when (featurePosition) {
            FeaturePosition.B0 -> begin
            FeaturePosition.B2 -> end
            FeaturePosition.O -> center().get()
            FeaturePosition.D0 -> evaluate(t)
            FeaturePosition.D1 -> far
            FeaturePosition.D2 -> evaluate(1 - t)
            FeaturePosition.D3 -> complement().far
            FeaturePosition.E0 -> get(FeaturePosition.O).divide(FastMath.sqrt(2.0), get(FeaturePosition.D0))
            FeaturePosition.E1 -> get(FeaturePosition.O).divide(FastMath.sqrt(2.0), get(FeaturePosition.D1))
            FeaturePosition.E2 -> get(FeaturePosition.O).divide(FastMath.sqrt(2.0), get(FeaturePosition.D2))
            FeaturePosition.E3 -> get(FeaturePosition.O).divide(FastMath.sqrt(2.0), get(FeaturePosition.D3))
        }
    }

    override fun linearCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<LinearFeaturePoints> = when{
        isOpen -> Stream.of(LinearFeaturePoints(conicSection.begin, conicSection.end))
        else -> Stream.of(LinearFeaturePoints(conicSection.far, conicSection.far))
    }

    override fun circularCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<CircularFeaturePoints> =
            when {
                conicSection.weight > 0.0 -> Stream.of(
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.O),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.D1),
                        Tuple3(FeaturePosition.D1, FeaturePosition.O, FeaturePosition.B0),
                        Tuple3(FeaturePosition.D1, FeaturePosition.O, FeaturePosition.B2)
                )
                isOpen -> Stream.of(
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.O),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.D1),

                        Tuple3(FeaturePosition.D1, FeaturePosition.O, FeaturePosition.D0),
                        Tuple3(FeaturePosition.D1, FeaturePosition.O, FeaturePosition.D2),

                        Tuple3(FeaturePosition.D0, FeaturePosition.D2, FeaturePosition.D1)
                )
                else -> Stream.of(
                        Tuple3(FeaturePosition.D0, FeaturePosition.D2, FeaturePosition.D1),
                        Tuple3(FeaturePosition.D0, FeaturePosition.D2, FeaturePosition.D3),
                        Tuple3(FeaturePosition.D1, FeaturePosition.D3, FeaturePosition.D0),
                        Tuple3(FeaturePosition.D1, FeaturePosition.D3, FeaturePosition.D2),

                        Tuple3(FeaturePosition.E0, FeaturePosition.E2, FeaturePosition.D1),
                        Tuple3(FeaturePosition.E0, FeaturePosition.E2, FeaturePosition.D3),
                        Tuple3(FeaturePosition.E1, FeaturePosition.E3, FeaturePosition.D0),
                        Tuple3(FeaturePosition.E1, FeaturePosition.E3, FeaturePosition.D2)
                )
            } .map { (p0, p1, pn) -> CircularFeaturePoints(conicSection[p0], conicSection[p1], conicSection[pn])}

    override fun ellipticCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<EllipticFeaturePoints> =
            when {
                conicSection.weight > 0.0 -> Stream.of(
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.O),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.E1)
                )
                isOpen -> Stream.of(
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.O),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.E1),
                        Tuple3(FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2),
                        Tuple3(FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2)
                )
                else -> Stream.of(
                        Tuple3(FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2),
                        Tuple3(FeaturePosition.D1, FeaturePosition.D2, FeaturePosition.D3),
                        Tuple3(FeaturePosition.D2, FeaturePosition.D3, FeaturePosition.D0),
                        Tuple3(FeaturePosition.D3, FeaturePosition.D0, FeaturePosition.D1),
                        Tuple3(FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2),
                        Tuple3(FeaturePosition.E1, FeaturePosition.E2, FeaturePosition.E3),
                        Tuple3(FeaturePosition.E2, FeaturePosition.E3, FeaturePosition.E0),
                        Tuple3(FeaturePosition.E3, FeaturePosition.E0, FeaturePosition.E1)
                )
            }.map { (f0, f1, f2) ->
                EllipticFeaturePoints(conicSection[f0], conicSection[f1], conicSection[f2])
            }
}
