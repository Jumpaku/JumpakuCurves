package jumpaku.curves.fsc.test.snap

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.core.test.transform.isCloseTo
import jumpaku.curves.fsc.snap.Grid
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Grid, expected: Grid, error: Double): Boolean =
    isCloseTo(actual.baseSpacingInWorld, expected.baseSpacingInWorld, error) &&
            actual.magnification == expected.magnification &&
            isCloseTo(actual.baseFuzzinessInWorld, expected.baseFuzzinessInWorld, error) &&
            isCloseTo(actual.baseGridToWorld, expected.baseGridToWorld, error)

fun closeTo(expected: Grid, precision: Double = 1.0e-9): TypeSafeMatcher<Grid> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }

