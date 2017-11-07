package jumpaku.fsc.snap.conicsection

import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.snap.point.PointSnapper

class LinearCandidateCreator(override val pointSnapper: PointSnapper): CandidateCreator {

    override fun createCandidate(conicSection: ConicSection): Stream<SnapCandidate> {
        val feature0 = FeaturePoint(conicSection.begin, FeatureType.Begin)
        val feature1 = FeaturePoint(conicSection.end, FeatureType.End)
        val (snapResults, toSnapped) = snapPoints2(feature0.point, feature1.point)
        return toSnapped.map {
            SnapCandidate(
                    Array.of(feature0, feature1), snapResults, it, conicSection.transform(it))
        } .toStream()
    }
}