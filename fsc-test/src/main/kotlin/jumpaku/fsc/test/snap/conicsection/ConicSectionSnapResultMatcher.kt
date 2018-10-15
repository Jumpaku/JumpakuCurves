package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.test.curve.rationalbezier.isCloseTo
import jumpaku.core.test.geom.isCloseTo
import jumpaku.core.test.isCloseTo
import jumpaku.fsc.snap.conicsection.ConicSectionSnapResult
import jumpaku.fsc.test.snap.point.isCloseTo
import org.amshove.kluent.should


fun isCloseTo(actual: ConicSectionSnapResult.Candidate, expected: ConicSectionSnapResult.Candidate, error: Double): Boolean =
        actual.featurePoints.size == expected.featurePoints.size &&
        actual.featurePoints.zip(expected.featurePoints).all { (a, e) ->
            isCloseTo(a.source, e.source, error) &&
                    if (a.target.isDefined && e.target.isDefined) isCloseTo(a.target.orThrow(), e.target.orThrow(), error)
                    else a.target.isDefined == e.target.isDefined

        }

fun isCloseTo(actual: ConicSectionSnapResult, expected: ConicSectionSnapResult, error: Double): Boolean =
        actual.snappedConicSection.isDefined == expected.snappedConicSection.isDefined &&
                actual.snappedConicSection.isEmpty ||
                (actual.snappedConicSection.isDefined &&
                        isCloseTo(actual.snappedConicSection.orThrow(), expected.snappedConicSection.orThrow(), error)) &&
                actual.candidates.size == expected.candidates.size &&
                actual.candidates.zip(expected.candidates).all { (a, e) ->
                    isCloseTo(a.grade.value, e.grade.value) &&
                            isCloseTo(a.candidate, e.candidate, error) }

fun ConicSectionSnapResult.shouldEqualToConicSectionSnapResult(expected: ConicSectionSnapResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}