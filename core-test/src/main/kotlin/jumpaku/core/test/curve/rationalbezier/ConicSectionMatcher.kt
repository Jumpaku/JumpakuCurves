package jumpaku.core.test.curve.rationalbezier

import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.test.curve.isCloseTo
import jumpaku.core.test.geom.isCloseTo
import jumpaku.core.test.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: ConicSection, expected: ConicSection, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.begin, expected.begin, error) &&
                isCloseTo(actual.far, expected.far, error) &&
                isCloseTo(actual.end, expected.end, error) &&
                isCloseTo(actual.weight, expected.weight, error)

fun ConicSection.shouldEqualToConicSection(expected: ConicSection, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}