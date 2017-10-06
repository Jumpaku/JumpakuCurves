package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.Tuple4
import io.vavr.collection.Stream
import io.vavr.collection.Array
import io.vavr.control.Option
import jumpaku.core.affine.*
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.fsc.snap.point.PointSnapResult
import jumpaku.fsc.snap.point.PointSnapper

enum class FeatureType { Begin, End, Center, Diameter0, Diameter1, Diameter2, Extra0, Extra1, Extra2, }

data class FeaturePoint(val point: Point, val type: FeatureType)

data class SnapCandidate(
        val featurePoints: Array<FeaturePoint>,
        val snappedFeatures: Array<PointSnapResult>,
        val toSnapped: Affine,
        val snappedConicSection: ConicSection) {
    init {
        require(featurePoints.size() == snappedFeatures.size()) {
            "featurePoints.size()(${featurePoints.size()}) != snappedFeatures.size()(${snappedFeatures.size()})"
        }
    }
}

interface CandidateCreator {

    val pointSnapper: PointSnapper

    fun createCandidate(conicSection: ConicSection): Stream<SnapCandidate>

    fun snapPoints2(featurePoint0: Point, featurePoint1: Point): Tuple2<Array<PointSnapResult>, Option<Affine>> {
        val results = Array.of(featurePoint0, featurePoint1).map { pointSnapper.snap(it) }
        val (s0, s1) = results.map { it.snappedPoint }
        return Tuple2(results, similarity(Tuple2(featurePoint0, featurePoint1), Tuple2(s0, s1)))
    }

    fun snapPoints2Normal(featurePoint0: Point, featurePoint1: Point, forNormal: Point): Tuple2<Array<PointSnapResult>, Option<Affine>> {
        val results = Array.of(featurePoint0, featurePoint1).map { pointSnapper.snap(it) }
        val (s0, s1) = results.map { it.snappedPoint }
        val pn = forNormal.normal(featurePoint0, featurePoint1).getOrElse(Vector())
        val sn = pointSnapper.snap(forNormal).snappedPoint.normal(s0, s1).getOrElse(Vector())
        return Tuple2(results, similarityWithNormal(Tuple3(featurePoint0, featurePoint1, pn), Tuple3(s0, s1, sn)))
    }

    fun snapPoints3(featurePoint0: Point, featurePoint1: Point, featurePoint2: Point): Tuple2<Array<PointSnapResult>, Option<Affine>> {
        val results = Array.of(featurePoint0, featurePoint1, featurePoint2).map { pointSnapper.snap(it) }
        val (s0, s1, s2) = results.map { it.snappedPoint }
        val n0 = featurePoint0.normal(featurePoint1, featurePoint2)
        val p3 = featurePoint0 + n0.getOrElse(Vector())
        val n1 = s0.normal(s1, s2)
        val s3 = s0 + n1.getOrElse(Vector())
        return Tuple2(results, calibrate(Tuple4(featurePoint0, featurePoint1, featurePoint2, p3), Tuple4(s0, s1, s2, s3)))
    }
}
