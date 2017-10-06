package jumpaku.fsc.snap

import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2

class LinearSnapper(override val pointSnapper: PointSnapper): ConicSectionSnapper {

    override fun snap(conicSection: ConicSection): Stream<ConicSectionSnapResult> {
        val feature0 = FeaturePoint(conicSection.begin, FeatureType.BeginEnd)
        val feature1 = FeaturePoint(conicSection.end, FeatureType.BeginEnd)
        val (snapResults, toSnapped) = snapPoints2(feature0.point, feature1.point)
        return toSnapped.map { ConicSectionSnapResult(
                Array.of(feature0, feature1), snapResults, it, conicSection.transform(it))
        } .toStream()
    }
}