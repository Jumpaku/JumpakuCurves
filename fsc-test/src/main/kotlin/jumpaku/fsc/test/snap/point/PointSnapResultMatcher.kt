package jumpaku.fsc.test.snap.point

import jumpaku.core.test.affine.isCloseTo
import jumpaku.core.test.isCloseTo
import jumpaku.fsc.snap.point.PointSnapResult
import jumpaku.fsc.test.snap.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: PointSnapResult, expected: PointSnapResult, error: Double): Boolean =
        actual.gridPoint == expected.gridPoint &&
                isCloseTo(actual.grade.value, expected.grade.value, error) &&
                isCloseTo(actual.worldPoint, expected.worldPoint, error) &&
                isCloseTo(actual.grid, expected.grid, error)

fun PointSnapResult.shouldBePointSnapResult(expected: PointSnapResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}