package jumpaku.fsc.test.snap

import jumpaku.core.test.curve.isCloseTo
import jumpaku.core.test.geom.isCloseTo
import jumpaku.core.test.isCloseTo
import jumpaku.fsc.snap.Grid
import org.amshove.kluent.should

fun isCloseTo(actual: Grid, expected: Grid, error: Double): Boolean =
        isCloseTo(actual.spacing, expected.spacing, error) &&
                actual.magnification == expected.magnification &&
                isCloseTo(actual.origin, expected.origin, error) &&
                isCloseTo(actual.rotation.axis, expected.rotation.axis, error) &&
                isCloseTo(actual.rotation.angleRadian, expected.rotation.angleRadian, error) &&
                isCloseTo(actual.fuzziness, expected.fuzziness, error) &&
                actual.resolution == expected.resolution

fun Grid.shouldEqualToGrid(expected: Grid, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}