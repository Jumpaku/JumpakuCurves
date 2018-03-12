package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.collection.Stream
import jumpaku.core.affine.Point
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.clamp
import org.apache.commons.math3.util.FastMath




class ConjugateCombinator : FeaturePointsCombinator {

    enum class FeaturePosition {
        B0, B2, O, D0, D1, D2, D3, E0, E1, E2, E3
    }

    operator fun ConicSection.get(featurePosition: FeaturePosition): Point = when{
        weight <= 0.0 -> {
            val t = clamp((1 - FastMath.sqrt((1 + weight) / (1 - weight))) / 2, 0.0, 0.5)
            when (featurePosition) {
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
        else -> complement()[featurePosition]
    }

    override fun linearCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<LinearFeaturePoints> = when{
        isOpen -> Stream.of(LinearFeaturePoints(conicSection.begin, conicSection.end))
        else -> Stream.of(LinearFeaturePoints(conicSection.far, conicSection.far))
    }

    override fun circularCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<CircularFeaturePoints> {
        val t = (1 - FastMath.sqrt((1 + conicSection.weight) / (1 - conicSection.weight))) / 2
        return when {
            t !in conicSection.domain -> Stream.of(
                    Tuple2(FeaturePosition.B0, FeaturePosition.B2),
                    Tuple2(FeaturePosition.B0, FeaturePosition.O),
                    Tuple2(FeaturePosition.B2, FeaturePosition.O),
                    Tuple2(FeaturePosition.D1, FeaturePosition.O))
            isOpen -> {
                val be = Stream.of(
                        Tuple2(FeaturePosition.B0, FeaturePosition.B2),
                        Tuple2(FeaturePosition.B0, FeaturePosition.O),
                        Tuple2(FeaturePosition.B2, FeaturePosition.O))
                val d = Stream.of(FeaturePosition.O, FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2)
                        .combinations(2).map { (a, b) -> Tuple2(a, b) }
                val e = Stream.of(FeaturePosition.O, FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2)
                        .combinations(2).map { (a, b) -> Tuple2(a, b) }
                be.appendAll(d).appendAll(e)
            }
            else -> {
                val d = Stream.of(FeaturePosition.O, FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2, FeaturePosition.D3)
                        .combinations(2).map { (a, b) -> Tuple2(a, b) }.toStream()
                val e = Stream.of(FeaturePosition.O, FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2, FeaturePosition.E3)
                        .combinations(2).map { (a, b) -> Tuple2(a, b) }.toStream()
                d.appendAll(e)
            }
        }.flatMap { (f0, f1) ->
            Stream.of(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1, FeaturePosition.O).map {
                CircularFeaturePoints(conicSection[f0], conicSection[f1], conicSection[it])
            }
        }
    }

    override fun ellipticCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<EllipticFeaturePoints> {
        val t = (1 - FastMath.sqrt((1 + conicSection.weight) / (1 - conicSection.weight))) / 2
        return when {
            t !in conicSection.domain -> Stream.of(
                    Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.O),
                    Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                    Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.E1),
                    Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.D1),
                    Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.E1),
                    Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.D1),
                    Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.E1))
            isOpen -> {
                val be = Stream.of(
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.O),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.B2, FeaturePosition.E1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.D2),
                        Tuple3(FeaturePosition.B0, FeaturePosition.D1, FeaturePosition.D2),
                        Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.D0),
                        Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B2, FeaturePosition.D0, FeaturePosition.D1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.E1),
                        Tuple3(FeaturePosition.B0, FeaturePosition.O, FeaturePosition.E2),
                        Tuple3(FeaturePosition.B0, FeaturePosition.E1, FeaturePosition.E2),
                        Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.E0),
                        Tuple3(FeaturePosition.B2, FeaturePosition.O, FeaturePosition.E1),
                        Tuple3(FeaturePosition.B2, FeaturePosition.E0, FeaturePosition.E1))
                val d = Stream.of(FeaturePosition.O, FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2)
                        .combinations(3).map { (a, b, c) -> Tuple3(a, b, c) }
                val e = Stream.of(FeaturePosition.O, FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2)
                        .combinations(3).map { (a, b, c) -> Tuple3(a, b, c) }
                be.appendAll(d).appendAll(e)
            }
            else -> {
                val d = Stream.of(FeaturePosition.O, FeaturePosition.D0, FeaturePosition.D1, FeaturePosition.D2, FeaturePosition.D3)
                        .combinations(3).map { (a, b, c) -> Tuple3(a, b, c) }.toStream()
                val e = Stream.of(FeaturePosition.O, FeaturePosition.E0, FeaturePosition.E1, FeaturePosition.E2, FeaturePosition.E3)
                        .combinations(3).map { (a, b, c) -> Tuple3(a, b, c) }.toStream()
                d.appendAll(e)
            }
        }.map { (f0, f1, f2) ->
            EllipticFeaturePoints(conicSection[f0], conicSection[f1], conicSection[f2])
        }
    }
}
