package jumpaku.curves.fsc.test.snap

import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.fsc.snap.Grid
import org.amshove.kluent.should

fun isCloseTo(actual: Grid, expected: Grid, error: Double): Boolean =
        isCloseTo(actual.baseSpacing, expected.baseSpacing, error) &&
                actual.magnification == expected.magnification &&
                isCloseTo(actual.origin, expected.origin, error) &&
                isCloseTo(actual.rotation.axis, expected.rotation.axis, error) &&
                isCloseTo(actual.rotation.angleRadian, expected.rotation.angleRadian, error) &&
                isCloseTo(actual.baseFuzziness, expected.baseFuzziness, error)

fun Grid.shouldEqualToGrid(expected: Grid, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}