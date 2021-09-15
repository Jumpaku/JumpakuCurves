package jumpaku.curves.fsc.snap.conicsection

import jumpaku.commons.control.Option
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.AffineTransform
import jumpaku.curves.fsc.snap.point.PointSnapResult

class ConicSectionSnapResult(val snappedConicSection: Option<ConicSection>, candidates: Iterable<EvaluatedCandidate>) {

    data class SnappedPoint(val source: Point, val target: Option<PointSnapResult>)

    class Candidate(featurePoints: Iterable<SnappedPoint>, val transform: AffineTransform) {

        val featurePoints: List<SnappedPoint> = featurePoints.toList()
    }

    data class EvaluatedCandidate(val grade: Grade, val candidate: Candidate)

    val candidates: List<EvaluatedCandidate> = candidates.toList()
}

