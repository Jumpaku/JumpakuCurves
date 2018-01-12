package jumpaku.fsc.snap.conicsection

import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.clamp
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.fsc.snap.point.PointSnapper
import org.apache.commons.math3.util.FastMath


class EllipticCandidateCreator(override val pointSnapper: PointSnapper): CandidateCreator {

    override fun createCandidate(conicSection: ConicSection): Stream<SnapCandidate> {
        val r = 1 - FastMath.sqrt(2.0)
        val w = clamp(conicSection.weight, -0.999, 0.999)
        val f = conicSection.far
        val o = conicSection.center().get()
        val e3 = FeaturePoint(o, FeatureType.Center)
        val d1 = FeaturePoint(f, FeatureType.Diameter1)
        val e1 = FeaturePoint(f.divide(r, o), FeatureType.Extra1)
        val b0 = FeaturePoint(conicSection.begin, FeatureType.Begin)
        val b2 = FeaturePoint(conicSection.end, FeatureType.End)
        val features = if (w > 0) {
            Stream.of(e3, d1, e1, b0, b2).combinations(3)
        } else {
            val t = (1 - FastMath.sqrt((1 + w) / (1 - w))) / 2
            val d0 = FeaturePoint(conicSection(t), FeatureType.Diameter0)
            val d2 = FeaturePoint(conicSection(1 - t), FeatureType.Diameter2)
            val e0 = FeaturePoint(d0.point.divide(r, o), FeatureType.Extra0)
            val e2 = FeaturePoint(d2.point.divide(r, o), FeatureType.Extra2)
            Stream.concat(
                    Stream.of(e3, d1, e1, b0, b2).combinations(3),
                    Stream.of(d0, d1, d2, e3).combinations(3),
                    Stream.of(e0, e1, e2, e3).combinations(3))
        }
        return features.flatMap { (f0, f1, f2) ->
            val (snapped, affine) = snapPoints3(f0.point, f1.point, f2.point)
            affine.map { SnapCandidate(Array.of(f0, f1, f2), snapped, it, conicSection.transform(it)) }
        }
    }
}