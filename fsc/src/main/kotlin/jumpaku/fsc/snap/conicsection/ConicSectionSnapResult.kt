package jumpaku.fsc.snap.conicsection

import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.affine.Affine
import jumpaku.core.affine.Point
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.fsc.snap.point.PointSnapResult

data class ConicSectionSnapResult(
        val candidate: Candidate,
        val grade: Grade,
        val candidates: Stream<Candidate>) {

    data class SnapPointPair(
            val cursor: Point,
            val snapped: PointSnapResult)

    data class Candidate(
            val featurePoints: Array<SnapPointPair>,
            val snapTransform: Affine,
            val snappedConicSection: ConicSection)
}