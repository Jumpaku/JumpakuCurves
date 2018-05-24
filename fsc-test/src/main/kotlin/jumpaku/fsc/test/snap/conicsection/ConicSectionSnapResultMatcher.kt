package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.test.curve.rationalbezier.isCloseTo
import jumpaku.core.test.geom.isCloseTo
import jumpaku.core.test.isCloseTo
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.snap.conicsection.ConicSectionSnapResult
import jumpaku.fsc.test.snap.point.isCloseTo
import org.amshove.kluent.should


fun isCloseTo(actual: ConicSectionSnapResult.Candidate, expected: ConicSectionSnapResult.Candidate, error: Double): Boolean =
        isCloseTo(actual.snappedConicSection, expected.snappedConicSection, error) &&
        actual.featurePoints.zip(expected.featurePoints).all { (a, e) ->
            isCloseTo(a.cursor, e.cursor, error) && isCloseTo(a.snapped, e.snapped, error)
        }

fun isCloseTo(actual: ConicSectionSnapResult, expected: ConicSectionSnapResult, error: Double): Boolean =
        actual.candidates.zip(expected.candidates).all { (a, e) -> isCloseTo(a, e, error) } &&
                isCloseTo(actual.candidate, expected.candidate, error) &&
                isCloseTo(actual.grade.value, expected.grade.value, error)

fun ConicSectionSnapResult.shouldEqualToConicSectionSnapResult(expected: ConicSectionSnapResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}