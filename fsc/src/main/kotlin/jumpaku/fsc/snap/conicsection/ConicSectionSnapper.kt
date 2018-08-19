package jumpaku.fsc.snap.conicsection

import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Try
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.transform.Calibrate
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.identify.reference.reparametrize
import jumpaku.fsc.identify.reparametrize
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.point.PointSnapper


class ConicSectionSnapper(val pointSnapper: PointSnapper, val featurePointsCombinator: FeaturePointsCombinator) {

    fun snap(
            grid: Grid,
            conicSection: ConicSection,
            curveClass: CurveClass,
            evaluator: (ConicSectionSnapResult.Candidate) -> Double = evaluateWithReference(conicSection)
    ): ConicSectionSnapResult {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section" }

        val candidates = enumerate(grid, conicSection, curveClass)
                .sortBy { candidate -> -evaluator(candidate) }.toArray()
        val snapped = candidates.headOption()
                .map { conicSection.transform(it.transform) }
                .getOrElse { conicSection.toCrisp() }
        return ConicSectionSnapResult(snapped, candidates)
    }

    fun enumerate(grid: Grid, conicSection: ConicSection, curveClass: CurveClass): Stream<ConicSectionSnapResult.Candidate> {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section" }
        return when {
            curveClass.isLinear -> enumerateLinearCandidate(grid, conicSection, curveClass.isOpen)
            curveClass.isCircular -> enumerateCircularCandidate(grid, conicSection, curveClass.isOpen)
            curveClass.isElliptic -> enumerateEllipticCandidate(grid, conicSection, curveClass.isOpen)
            else -> error("")
        }
    }

    fun enumerateLinearCandidate(grid: Grid, conicSection: ConicSection, isOpen: Boolean): Stream<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.linearCombinations(conicSection, isOpen).flatMap { (f0, f1) ->
                val s0 = pointSnapper.snap(grid, f0)
                val s1 = pointSnapper.snap(grid, f1)
                Try.ofSupplier {
                    val calibrate = Calibrate(
                            f0 to s0.map { it.worldPoint(grid) }.getOrElse { f0 },
                            f1 to s1.map { it.worldPoint(grid) }.getOrElse { f1 })
                    ConicSectionSnapResult.Candidate(
                            Array.of(ConicSectionSnapResult.SnappedPoint(f0, s0), ConicSectionSnapResult.SnappedPoint(f1, s1)),
                            calibrate)
                }
            }

    fun enumerateCircularCandidate(grid: Grid, conicSection: ConicSection, isOpen: Boolean): Stream<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.circularCombinations(conicSection, isOpen).flatMap { (f0, f1, fn) ->
                val s0 = pointSnapper.snap(grid, f0)
                val s1 = pointSnapper.snap(grid, f1)
                val sn = pointSnapper.snap(grid, fn)
                val n = sn.map { it.worldPoint(grid) }.getOrElse { fn }
                        .normal(s0.map { it.worldPoint(grid) }.getOrElse { f0 },
                                s1.map { it.worldPoint(grid) }.getOrElse { f1 })
                val m = fn.normal(f0, f1)
                Try.ofSupplier {
                    val calibrate = Calibrate.similarityWithNormal(
                            f0 to s0.map { it.worldPoint(grid) }.getOrElse { f0 },
                            f1 to s1.map { it.worldPoint(grid) }.getOrElse { f1 },
                            m.get() to n.get())
                    ConicSectionSnapResult.Candidate(
                            Array.of(ConicSectionSnapResult.SnappedPoint(f0, s0), ConicSectionSnapResult.SnappedPoint(f1, s1)),
                            calibrate)
                }.toOption()
            }

    fun enumerateEllipticCandidate(grid: Grid, conicSection: ConicSection, isOpen: Boolean): Stream<ConicSectionSnapResult.Candidate> =
            featurePointsCombinator.ellipticCombinations(conicSection, isOpen).flatMap { (f0, f1, f2) ->
                val s0 = pointSnapper.snap(grid, f0)
                val s1 = pointSnapper.snap(grid, f1)
                val s2 = pointSnapper.snap(grid, f2)
                Try.ofSupplier {
                    val calibrate = Calibrate(f0 to s0.map { it.worldPoint(grid) }.getOrElse { f0 },
                            f1 to s1.map { it.worldPoint(grid) }.getOrElse { f1 },
                            f2 to s2.map { it.worldPoint(grid) }.getOrElse { f2 })
                    ConicSectionSnapResult.Candidate(Array.of(
                            ConicSectionSnapResult.SnappedPoint(f0, s0),
                            ConicSectionSnapResult.SnappedPoint(f1, s1),
                            ConicSectionSnapResult.SnappedPoint(f2, s2)),
                            calibrate)
                }
            }

    companion object {

        fun evaluateWithFsc(fsc: BSpline, original: ConicSection, nFmps: Int = 15): (ConicSectionSnapResult.Candidate) -> Double = {
            reparametrize(original.transform(it.transform)).isPossible(reparametrize(fsc), nFmps).value
        }

        fun evaluateWithReference(original: ConicSection, nFmps: Int = 15): (ConicSectionSnapResult.Candidate) -> Double = {
            reparametrize(original.transform(it.transform)).isPossible(reparametrize(original), nFmps).value
        }
    }
}
