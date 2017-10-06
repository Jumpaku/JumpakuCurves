package jumpaku.fsc.snap

import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.clamp
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.util.FastMath

class CircularSnapper(override val pointSnapper: PointSnapper): ConicSectionSnapper {

    override fun snap(conicSection: ConicSection): Stream<ConicSectionSnapResult> {
        val r = 1 - FastMath.sqrt(2.0)
        val w = clamp(conicSection.weight, -0.999, 0.999)
        val f = conicSection.far
        val o = conicSection.center()
        val e3 = FeaturePoint(o, FeatureType.Extra)
        val d1 = FeaturePoint(f, FeatureType.Diameter)
        val e1 = FeaturePoint(f.divide(r, o), FeatureType.Extra)
        val b0 = FeaturePoint(conicSection.begin, FeatureType.BeginEnd)
        val b2 = FeaturePoint(conicSection.end, FeatureType.BeginEnd)
        val features = if (w > 0) {
            Stream.of(d1, e1, b0, b2).combinations(3)
        } else {
            val t = (1 - FastMath.sqrt((1 + w) / (1 - w))) / 2
            val d0 = FeaturePoint(conicSection(t), FeatureType.Diameter)
            val d2 = FeaturePoint(conicSection(1 - t), FeatureType.Diameter)
            val e0 = FeaturePoint(d0.point.divide(r, o), FeatureType.Extra)
            val e2 = FeaturePoint(d2.point.divide(r, o), FeatureType.Extra)
            Stream.of(d0, d1, d2, e3).combinations(3).appendAll(Stream.of(e0, e1, e2, e3).combinations(3))
        }
        return features.flatMap { (f0, f1, f2) ->
            Stream.of(Stream.of(f0, f1, f2), Stream.of(f0, f1, f2), Stream.of(f0, f1, f2)).flatMap { (a, b, n) ->
                val (snapped, affine) = snapPoints2Normal(a.point, b.point, n.point)
                affine.map { ConicSectionSnapResult(Array.of(a, b), snapped, it, conicSection.transform(it)) }
            }
        }
    }
}
