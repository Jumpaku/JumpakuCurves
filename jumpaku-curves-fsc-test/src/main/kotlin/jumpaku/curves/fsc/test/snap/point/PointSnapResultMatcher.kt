package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.test.isCloseTo
import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.snap.point.PointSnapResult
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: PointSnapResult, expected: PointSnapResult, error: Double): Boolean =
        actual.gridPoint == expected.gridPoint &&
                actual.resolution == expected.resolution &&
                isCloseTo(actual.grade.value, expected.grade.value, error)

fun closeTo(expected: PointSnapResult, precision: Double = 1.0e-9): TypeSafeMatcher<PointSnapResult> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

