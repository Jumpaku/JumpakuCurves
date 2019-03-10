package jumpaku.curves.fsc.snap.conicsection

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.orDefault
import jumpaku.commons.control.result
import jumpaku.commons.control.toOption
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.transform.Calibrate
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.point.PointSnapper


class ConicSectionSnapper<C : FeaturePointsCombinator>(
        val pointSnapper: PointSnapper,
        val featurePointsCombinator: C
) : ToJson {

    fun snap(
            grid: Grid,
            conicSection: ConicSection,
            curveClass: CurveClass,
            evaluator: (ConicSectionSnapResult.Candidate) -> Grade = evaluateWithReference(conicSection)
    ): ConicSectionSnapResult {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section" }

        val candidates = enumerate(grid, conicSection, curveClass)
                .map { ConicSectionSnapResult.EvaluatedCandidate(evaluator(it), it) }
                .sortedByDescending { it.grade }
        val snapped = candidates.firstOrNull()
                .toOption()
                .filter { it.grade.value > 0.0 }
                .map { conicSection.transform(it.candidate.transform) }
        return ConicSectionSnapResult(snapped, candidates)
    }

    fun enumerate(grid: Grid, conicSection: ConicSection, curveClass: CurveClass)
            : List<ConicSectionSnapResult.Candidate> {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section" }
        return when {
            curveClass.isLinear -> enumerateLinearCandidate(grid, conicSection, curveClass.isOpen)
            curveClass.isCircular -> enumerateCircularCandidate(grid, conicSection, curveClass.isOpen)
            curveClass.isElliptic -> enumerateEllipticCandidate(grid, conicSection, curveClass.isOpen)
            else -> error("")
        }
    }

    fun enumerateLinearCandidate(
            grid: Grid, conicSection: ConicSection, isOpen: Boolean
    ): List<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.linearCombinations(conicSection, isOpen).flatMap { (f0, f1) ->
                val s0 = pointSnapper.snap(grid, f0)
                val s1 = pointSnapper.snap(grid, f1)
                result {
                    val calibrate = Calibrate(
                            f0 to s0.map { it.worldPoint(grid) }.orDefault { f0 },
                            f1 to s1.map { it.worldPoint(grid) }.orDefault { f1 })
                    ConicSectionSnapResult.Candidate(
                            listOf(ConicSectionSnapResult.SnappedPoint(f0, s0), ConicSectionSnapResult.SnappedPoint(f1, s1)),
                            calibrate)
                }.value()
            }

    fun enumerateCircularCandidate(
            grid: Grid, conicSection: ConicSection, isOpen: Boolean
    ): List<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.circularCombinations(conicSection, isOpen).flatMap { (f0, f1, fn) ->
                val s0 = pointSnapper.snap(grid, f0)
                val s1 = pointSnapper.snap(grid, f1)
                val sn = pointSnapper.snap(grid, fn)
                val n = sn.map { it.worldPoint(grid) }.orDefault { fn }
                        .normal(s0.map { it.worldPoint(grid) }.orDefault { f0 },
                                s1.map { it.worldPoint(grid) }.orDefault { f1 })
                val m = fn.normal(f0, f1)
                result {
                    val calibrate = Calibrate.similarityWithNormal(
                            f0 to s0.map { it.worldPoint(grid) }.orDefault { f0 },
                            f1 to s1.map { it.worldPoint(grid) }.orDefault { f1 },
                            m.orThrow() to n.orThrow())
                    ConicSectionSnapResult.Candidate(
                            listOf(ConicSectionSnapResult.SnappedPoint(f0, s0), ConicSectionSnapResult.SnappedPoint(f1, s1)),
                            calibrate)
                }.value()
            }

    fun enumerateEllipticCandidate(
            grid: Grid, conicSection: ConicSection, isOpen: Boolean
    ): List<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.ellipticCombinations(conicSection, isOpen).flatMap { (f0, f1, f2) ->
                val s0 = pointSnapper.snap(grid, f0)
                val s1 = pointSnapper.snap(grid, f1)
                val s2 = pointSnapper.snap(grid, f2)
                result {
                    val calibrate = Calibrate(f0 to s0.map { it.worldPoint(grid) }.orDefault { f0 },
                            f1 to s1.map { it.worldPoint(grid) }.orDefault { f1 },
                            f2 to s2.map { it.worldPoint(grid) }.orDefault { f2 })
                    ConicSectionSnapResult.Candidate(listOf(
                            ConicSectionSnapResult.SnappedPoint(f0, s0),
                            ConicSectionSnapResult.SnappedPoint(f1, s1),
                            ConicSectionSnapResult.SnappedPoint(f2, s2)),
                            calibrate)
                }.value()
            }

    override fun toJson(): JsonElement {
        check(featurePointsCombinator is ConjugateCombinator) { "cannot convert to JSON" }
        return jsonObject(
                "pointSnapper" to pointSnapper.toJson(),
                "featurePointsCombinator" to jsonObject("type" to "ConjugateCombinator".toJson()))
    }

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): ConicSectionSnapper<ConjugateCombinator> {
            check(json["featurePointsCombinator"]["type"].string == "ConjugateCombinator") {
                "invalid featurePointsCombinator type ${json["featurePointsCombinator"]["type"].string}"
            }
            return ConicSectionSnapper(
                    PointSnapper.fromJson(json["pointSnapper"]), ConjugateCombinator)
        }

        fun evaluateWithFsc(fsc: BSpline, original: ConicSection, nFmps: Int = 15)
                : (ConicSectionSnapResult.Candidate) -> Grade = {
            reparametrize(original.transform(it.transform)).isPossible(reparametrize(fsc), nFmps)
        }

        fun evaluateWithReference(original: ConicSection, nFmps: Int = 15)
                : (ConicSectionSnapResult.Candidate) -> Grade = {
            reparametrize(original.transform(it.transform)).isPossible(reparametrize(original), nFmps)
        }
    }
}
