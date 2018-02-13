package jumpaku.fsc.snap.conicsection.candidate

import io.vavr.API
import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.collection.Map
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.*
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.*
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.snap.conicsection.FeatureType
import jumpaku.fsc.snap.point.PointSnapper


class CircularCandidateCreator(
        override val pointSnapper: PointSnapper,
        val featureCombinations: (ConicSection, Boolean)->Stream<Tuple2<FeatureType, FeatureType>> = Companion::featureCombinations
): CandidateCreator {

    fun transform(featureTypes: Tuple3<FeatureType, FeatureType, FeatureType>,
                  featurePoints: Map<FeatureType, Point>,
                  unitFeaturePoints: Map<FeatureType, Point>): Option<Affine> {
        val (p0, p1, pn) = featureTypes.toSeq().map { featurePoints[it as FeatureType].get() }
        val (up0, up1, upn) = featureTypes.toSeq().map { unitFeaturePoints[it as FeatureType].get() }
        val n = pn.normal(p0, p1).getOrElse(Vector())
        val un = upn.normal(up0, up1).getOrElse(Vector())
        return similarityWithNormal(Tuple3(up0, up1, un), Tuple3(p0, p1, n))
    }

    override fun create(conicSection: ConicSection, isOpen: Boolean): Stream<SnapCandidate> {
        val unitArc = unitCircularArc(conicSection.weight)
        val unitFeaturePoints = unitArc.featurePoints(CurveClass.CircularArc)
        val snappedFeaturePointResults = conicSection.featurePoints(CurveClass.CircularArc)
                .mapValues { pointSnapper.snap(it) }
        val snappedFeatureWorldPoints = snappedFeaturePointResults.mapValues { it.worldPoint }
        val types = featureCombinations(conicSection, isOpen)

        return API.For(types, Stream.of(FeatureType.Center, FeatureType.Diameter1, FeatureType.Begin, FeatureType.End))
                .`yield` { (t0, t1), tn -> Tuple3(t0, t1, tn) }
                .flatMap { featureTypes ->
                    val (t0, t1) = featureTypes
                    transform(featureTypes, snappedFeatureWorldPoints, unitFeaturePoints).map {
                        SnapCandidate(snappedFeaturePointResults.filterKeys { it in listOf(t0, t1) }, unitArc.transform(it))
                    }
                }.toStream()
    }

    companion object {
        fun featureCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<Tuple2<FeatureType, FeatureType>> = when {
            conicSection.weight >= 0.0 -> {
                Stream.of(
                        Tuple2(FeatureType.Center, FeatureType.Begin),
                        Tuple2(FeatureType.Center, FeatureType.End),
                        Tuple2(FeatureType.Begin, FeatureType.End),
                        Tuple2(FeatureType.Center, FeatureType.Diameter1),
                        Tuple2(FeatureType.Center, FeatureType.Extra1))
            }
            isOpen -> {
                val be = Stream.of(
                        Stream.of(FeatureType.Center, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.End),
                        Stream.of(FeatureType.Begin, FeatureType.End))
                val e = Stream.of(FeatureType.Center, FeatureType.Extra0, FeatureType.Extra1, FeatureType.Extra2)
                        .combinations(2)
                val d = Stream.of(FeatureType.Center, FeatureType.Diameter0, FeatureType.Diameter1, FeatureType.Diameter2)
                        .combinations(2)
                Stream.concat(be, e, d).map { (t0, t1) -> Tuple2(t0, t1) }
            }
            else -> {
                val be = Stream.of(
                        Stream.of(FeatureType.Center, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.End),
                        Stream.of(FeatureType.Begin, FeatureType.End))
                val e = Stream.of(FeatureType.Center, FeatureType.Extra0, FeatureType.Extra1, FeatureType.Extra2, FeatureType.Extra3)
                        .combinations(2)
                val d = Stream.of(FeatureType.Center, FeatureType.Diameter0, FeatureType.Diameter1, FeatureType.Diameter2, FeatureType.Diameter3)
                        .combinations(2)
                Stream.concat(be, e, d).map { (t0, t1) -> Tuple2(t0, t1) }
            }
        }
    }
}
