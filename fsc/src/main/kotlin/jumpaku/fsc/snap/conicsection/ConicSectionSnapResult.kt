package jumpaku.fsc.snap.conicsection

import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.affine.Affine
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.fsc.snap.point.PointSnapResult

data class ConicSectionSnapResult(
        val candidate: Candidate,
        val grade: Grade,
        val candidates: Stream<Candidate>) {
    data class Candidate(
            val featurePoints: Array<PointSnapResult>,
            val snapTransform: Affine,
            val snappedConicSection: ConicSection)
}