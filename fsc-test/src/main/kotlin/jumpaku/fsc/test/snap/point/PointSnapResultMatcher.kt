package jumpaku.fsc.test.snap.point

import jumpaku.core.test.isCloseTo
import jumpaku.fsc.snap.point.PointSnapResult
import org.amshove.kluent.should

fun isCloseTo(actual: PointSnapResult, expected: PointSnapResult, error: Double): Boolean =
        actual.gridPoint == expected.gridPoint &&
                actual.resolution == expected.resolution &&
                isCloseTo(actual.grade.value, expected.grade.value, error)

fun PointSnapResult.shouldEqualToPointSnapResult(expected: PointSnapResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}