package jumpaku.curves.fsc.test.snap

import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.commons.test.isCloseTo
import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.snap.Grid
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Grid, expected: Grid, error: Double): Boolean =
        isCloseTo(actual.baseSpacing, expected.baseSpacing, error) &&
                actual.magnification == expected.magnification &&
                isCloseTo(actual.origin, expected.origin, error) &&
                isCloseTo(actual.rotation.axis, expected.rotation.axis, error) &&
                isCloseTo(actual.rotation.angleRadian, expected.rotation.angleRadian, error) &&
                isCloseTo(actual.baseFuzziness, expected.baseFuzziness, error)

fun closeTo(expected: Grid, precision: Double = 1.0e-9): TypeSafeMatcher<Grid> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

