package jumpaku.fsc.snap.conicsection.candidate

import io.vavr.collection.Map
import io.vavr.collection.Stream
import jumpaku.core.affine.Point
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.hashMap
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.snap.conicsection.FeatureType
import jumpaku.fsc.snap.point.PointSnapResult
import jumpaku.fsc.snap.point.PointSnapper
import org.apache.commons.math3.util.FastMath

fun unitCircularArc(weight: Double): ConicSection = ConicSection(
            Point.xy(-FastMath.sqrt(1-weight*weight), weight),
            Point.xy(0.0, 1.0),
            Point.xy(FastMath.sqrt(1-weight*weight), weight),
            weight)

fun ConicSection.featurePoints(curveClass: CurveClass): Map<FeatureType, Point> {
    require(curveClass.isConicSection) {
        "curveClass($curveClass) must be not FreeCurve"
    }
    return when {
        curveClass.isLinear -> hashMap(
                FeatureType.Begin to this.begin,
                FeatureType.Diameter1 to this.far,
                FeatureType.End to this.end)
        else -> {
            val o = center().get()
            val t = (1 - FastMath.sqrt((1 + weight) / (1 - weight))) / 2
            val r2 = FastMath.sqrt(2.0)
            val d1 = evaluate(0.5)
            when {
                t !in domain -> {
                    hashMap(
                            FeatureType.Begin to this.begin,
                            FeatureType.End to this.end,
                            FeatureType.Center to o,
                            FeatureType.Diameter1 to d1,
                            FeatureType.Extra1 to o.divide(r2, d1))
                }
                curveClass.isOpen -> {
                    val d0 = evaluate(t)
                    val d2 = evaluate(1 - t)
                    hashMap(
                            FeatureType.Begin to this.begin,
                            FeatureType.End to this.end,
                            FeatureType.Center to o,
                            FeatureType.Diameter0 to d0,
                            FeatureType.Diameter1 to d1,
                            FeatureType.Diameter2 to d2,
                            FeatureType.Extra0 to o.divide(r2, d0),
                            FeatureType.Extra1 to o.divide(r2, d1),
                            FeatureType.Extra2 to o.divide(r2, d2))
                }
                else ->{
                    val d0 = evaluate(t)
                    val d2 = evaluate(1 - t)
                    val d3 = complement().far
                    hashMap(
                            FeatureType.Begin to this.begin,
                            FeatureType.End to this.end,
                            FeatureType.Center to o,
                            FeatureType.Diameter0 to d0,
                            FeatureType.Diameter1 to d1,
                            FeatureType.Diameter2 to d2,
                            FeatureType.Diameter3 to d3,
                            FeatureType.Extra0 to o.divide(r2, d0),
                            FeatureType.Extra1 to o.divide(r2, d1),
                            FeatureType.Extra2 to o.divide(r2, d2),
                            FeatureType.Extra3 to o.divide(r2, d3))
                }
            }
            }

    }
}

data class SnapCandidate(
        val features: Map<FeatureType, PointSnapResult>,
        val conicSection: ConicSection)

interface CandidateCreator {

    val pointSnapper: PointSnapper

    fun create(conicSection: ConicSection, isOpen: Boolean): Stream<SnapCandidate>
}
