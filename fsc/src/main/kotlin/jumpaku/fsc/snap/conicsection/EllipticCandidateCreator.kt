package jumpaku.fsc.snap.conicsection

import io.vavr.API
import io.vavr.Tuple3
import io.vavr.Tuple4
import io.vavr.collection.Map
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.*
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.snap.point.PointSnapper


class EllipticCandidateCreator(
        override val pointSnapper: PointSnapper,
        val featureCombinations: (ConicSection, Boolean)->Stream<Tuple3<FeatureType, FeatureType, FeatureType>>
): CandidateCreator {

    fun transform(featureTypes: Tuple3<FeatureType, FeatureType, FeatureType>,
                  featurePoints: Map<FeatureType, Point>,
                  unitFeaturePoints: Map<FeatureType, Point>): Option<Affine> {
        val (p0, p1, p2) = featureTypes.toSeq().map { featurePoints[it as FeatureType].get() }
        val (up0, up1, up2) = featureTypes.toSeq().map { unitFeaturePoints[it as FeatureType].get() }
        val n = p0.normal(p1, p2).getOrElse(Vector())
        val un = up0.normal(up1, up2).getOrElse(Vector())
        return calibrate(Tuple4(up0, up1, up2, up0 + un), Tuple4(p0, p1, p2, p0+n))
    }

    override fun create(conicSection: ConicSection, isOpen: Boolean): Stream<SnapCandidate> {
        val unitArc = unitCircularArc(conicSection.weight)
        val unitFeaturePoints = unitArc.featurePoints(CurveClass.EllipticArc)
        val snappedFeaturePointsResult = conicSection.featurePoints(CurveClass.EllipticArc)
                .mapValues { pointSnapper.snap(it) }
        val snappedFeatureWorldPoints = snappedFeaturePointsResult.mapValues { it.worldPoint }
        val types = featureCombinations(conicSection, isOpen)

        return types.flatMap { featureTypes ->
            val (t0, t1, t2) = featureTypes
            transform(featureTypes, snappedFeatureWorldPoints, unitFeaturePoints).map {
                SnapCandidate(snappedFeaturePointsResult.filterKeys { it in listOf(t0, t1, t2) }, unitArc.transform(it))
            }
        }.toStream()
    }

    companion object {
        fun featureCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<Tuple3<FeatureType, FeatureType, FeatureType>> = when {
            conicSection.weight >= 0 -> {
                Stream.of(
                        Tuple3(FeatureType.Center, FeatureType.Begin, FeatureType.End),
                        Tuple3(FeatureType.Diameter1, FeatureType.Begin, FeatureType.End),
                        Tuple3(FeatureType.Extra1, FeatureType.Begin, FeatureType.End),
                        Tuple3(FeatureType.Center, FeatureType.Diameter1, FeatureType.Begin),
                        Tuple3(FeatureType.Center, FeatureType.Diameter1, FeatureType.End),
                        Tuple3(FeatureType.Center, FeatureType.Extra1, FeatureType.Begin),
                        Tuple3(FeatureType.Center, FeatureType.Extra1, FeatureType.End))
            }
            isOpen -> {
                val be = Stream.of(
                        Stream.of(FeatureType.Center, FeatureType.Begin, FeatureType.End),
                        Stream.of(FeatureType.Diameter1, FeatureType.Begin, FeatureType.End),
                        Stream.of(FeatureType.Extra1, FeatureType.Begin, FeatureType.End),
                        Stream.of(FeatureType.Center, FeatureType.Diameter1, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.Diameter1, FeatureType.End),
                        Stream.of(FeatureType.Center, FeatureType.Extra1, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.Extra1, FeatureType.End))
                val e = Stream.of(FeatureType.Center, FeatureType.Extra0, FeatureType.Extra1, FeatureType.Extra2)
                        .combinations(3)
                val d = Stream.of(FeatureType.Center, FeatureType.Diameter0, FeatureType.Diameter1, FeatureType.Diameter2)
                        .combinations(3)
                Stream.concat(be, e, d)
                        .map { (t0, t1, t2) -> Tuple3(t0, t1, t2) }
            }
            else -> {
                val be = Stream.of(
                        Stream.of(FeatureType.Center, FeatureType.Begin, FeatureType.End),
                        Stream.of(FeatureType.Diameter1, FeatureType.Begin, FeatureType.End),
                        Stream.of(FeatureType.Extra1, FeatureType.Begin, FeatureType.End),
                        Stream.of(FeatureType.Center, FeatureType.Diameter1, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.Diameter1, FeatureType.End),
                        Stream.of(FeatureType.Center, FeatureType.Extra1, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.Extra1, FeatureType.End),
                        Stream.of(FeatureType.Center, FeatureType.Diameter3, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.Diameter3, FeatureType.End),
                        Stream.of(FeatureType.Center, FeatureType.Extra3, FeatureType.Begin),
                        Stream.of(FeatureType.Center, FeatureType.Extra3, FeatureType.End))
                val e = Stream.of(FeatureType.Center, FeatureType.Extra0, FeatureType.Extra1, FeatureType.Extra2, FeatureType.Extra3)
                        .combinations(3)
                val d = Stream.of(FeatureType.Center, FeatureType.Diameter0, FeatureType.Diameter1, FeatureType.Diameter2, FeatureType.Diameter3)
                        .combinations(3)
                Stream.concat(be, e, d).map { (t0, t1, t2) -> Tuple3(t0, t1, t2) }
            }
        }
    }
}