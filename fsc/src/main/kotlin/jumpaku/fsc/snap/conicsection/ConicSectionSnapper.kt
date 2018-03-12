package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.affine.*
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.snap.point.PointSnapper


class ConicSectionSnapper(val pointSnapper: PointSnapper, val featurePointsCombinator: FeaturePointsCombinator){

    fun snap(conicSection: ConicSection, curveClass: CurveClass, evaluator: (ConicSectionSnapResult.Candidate)-> Grade): ConicSectionSnapResult {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section"}

        val candidates = enumerate(conicSection, curveClass)
        val (candidate, value) = candidates.map { Tuple2(it, evaluator(it)) }
                .maxBy { (_, value) -> value }.get()
        return ConicSectionSnapResult(candidate, value, candidates)
    }

    fun enumerate(conicSection: ConicSection, curveClass: CurveClass): Stream<ConicSectionSnapResult.Candidate> {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section" }
        return when {
            curveClass.isLinear -> enumerateLinearCandidate(conicSection, curveClass.isOpen)
            curveClass.isCircular -> enumerateCircularCandidate(conicSection, curveClass.isOpen)
            curveClass.isElliptic -> enumerateEllipticCandidate(conicSection, curveClass.isOpen)
            else -> error("")
        }
    }

    fun enumerateLinearCandidate(conicSection: ConicSection, isOpen: Boolean): Stream<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.linearCombinations(conicSection, isOpen).flatMap { (f0, f1) ->
                val s0 = pointSnapper.snap(f0)
                val s1 = pointSnapper.snap(f1)
                similarity(Tuple2(f0, f1), Tuple2(s0.worldPoint, s1.worldPoint)).map {
                    ConicSectionSnapResult.Candidate(Array.of(s0, s1), it, conicSection.transform(it))
                }
            }

    fun enumerateCircularCandidate(conicSection: ConicSection, isOpen: Boolean): Stream<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.circularCombinations(conicSection, isOpen).flatMap { (f0, f1, fn) ->
                val s0 = pointSnapper.snap(f0)
                val s1 = pointSnapper.snap(f1)
                fn.normal(f0, f1).flatMap {
                    similarityWithNormal(Tuple3(f0, f1, it), Tuple3(s0.worldPoint, s1.worldPoint, Vector())).map {
                        ConicSectionSnapResult.Candidate(Array.of(s0, s1), it, conicSection.transform(it))
                    }
                }
            }

    fun enumerateEllipticCandidate(conicSection: ConicSection, isOpen: Boolean): Stream<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.ellipticCombinations(conicSection, isOpen).flatMap { (f0, f1, f2) ->
                val s0 = pointSnapper.snap(f0)
                val s1 = pointSnapper.snap(f1)
                val s2 = pointSnapper.snap(f2)
                calibrateToPlane(Tuple3(f0, f1, f2), Tuple3(s0.worldPoint, s1.worldPoint, s2.worldPoint)).map {
                    ConicSectionSnapResult.Candidate(Array.of(s0, s1, s2), it, conicSection.transform(it))
                }
            }
}
