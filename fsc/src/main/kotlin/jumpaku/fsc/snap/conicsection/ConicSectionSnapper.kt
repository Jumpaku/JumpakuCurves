package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.collection.Stream
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.snap.point.PointSnapper


data class ConicSectionSnapResult(
        val candidate: SnapCandidate,
        val grade: Grade,
        val candidates: Stream<SnapCandidate>)

class ConicSectionSnapper(
        pointSnapper: PointSnapper,
        linearFeatureCombinations:
            (ConicSection, Boolean) ->Stream<Tuple2<FeatureType, FeatureType>> = LinearCandidateCreator.Companion::featureCombinations,
        circularFeatureCombinations:
            (ConicSection, Boolean)->Stream<Tuple2<FeatureType, FeatureType>> = CircularCandidateCreator.Companion::featureCombinations,
        ellipticFeatureCombinations:
            (ConicSection, Boolean)->Stream<Tuple3<FeatureType, FeatureType, FeatureType>> = EllipticCandidateCreator.Companion::featureCombinations) {

    val linearCandidateCreator: LinearCandidateCreator = LinearCandidateCreator(pointSnapper, linearFeatureCombinations)

    val circularCandidateCreator: CircularCandidateCreator = CircularCandidateCreator(pointSnapper, circularFeatureCombinations)

    val ellipticCandidateCreator: EllipticCandidateCreator = EllipticCandidateCreator(pointSnapper, ellipticFeatureCombinations)

    val candidateSelector: CandidateSelector = CandidateSelector()

    fun snap(conicSection: ConicSection, curveClass: CurveClass, evaluator: (SnapCandidate)-> Grade): ConicSectionSnapResult {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section"}
        val candidates = when {
            curveClass.isLinear -> linearCandidateCreator.create(conicSection, curveClass.isOpen)
            curveClass.isCircular -> circularCandidateCreator.create(conicSection, curveClass.isOpen)
            else -> ellipticCandidateCreator.create(conicSection, curveClass.isOpen)
        }
        val (candidate, value) = candidateSelector.select(candidates, evaluator)
        return ConicSectionSnapResult(candidate, value, candidates)
    }
}
