package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple2
import io.vavr.collection.Stream
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.snap.point.PointSnapper

class LinearCandidateCreator(
        override val pointSnapper: PointSnapper,
        val featureCombinations: (ConicSection, Boolean)->Stream<Tuple2<FeatureType, FeatureType>> = Companion::featureCombinations
): CandidateCreator {

    override fun create(conicSection: ConicSection, isOpen: Boolean): Stream<SnapCandidate> {
        val snappedFeaturePointResults = conicSection.featurePoints(CurveClass.LineSegment)
                .mapValues { pointSnapper.snap(it) }
        val snappedFeatureWorldPoints = snappedFeaturePointResults.mapValues { it.worldPoint }
        val types = featureCombinations(conicSection, isOpen)
        return types.map { (t0, t1) ->
            SnapCandidate(snappedFeaturePointResults.filterKeys { it in listOf(t0, t1) },
                    ConicSection.lineSegment(snappedFeatureWorldPoints[t0].get(), snappedFeatureWorldPoints[t1].get()))
        }

    }
    companion object {
        fun featureCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<Tuple2<FeatureType, FeatureType>> = when {
            isOpen -> Stream.of(Tuple2(FeatureType.Begin, FeatureType.End))
            else -> Stream.of(Tuple2(FeatureType.Diameter1, FeatureType.Diameter1))

        }
    }
}