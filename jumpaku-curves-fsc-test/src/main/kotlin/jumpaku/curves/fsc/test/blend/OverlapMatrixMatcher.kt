package jumpaku.curves.fsc.test.blend

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.fsc.blend.OverlapMatrix
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: OverlapMatrix, expected: OverlapMatrix, error: Double = 1.0e-9): Boolean =
    actual.rowSize == expected.rowSize &&
            actual.columnSize == expected.columnSize &&
            actual.zip(expected).all { (a, e) -> isCloseTo(a.value, e.value, error) }

fun closeTo(expected: OverlapMatrix, precision: Double = 1.0e-9): TypeSafeMatcher<OverlapMatrix> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }
