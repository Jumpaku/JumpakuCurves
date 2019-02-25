package jumpaku.curves.core.test.curve.rationalbezier

import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.test.matcher
import org.amshove.kluent.should
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: ConicSection, expected: ConicSection, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.begin, expected.begin, error) &&
                isCloseTo(actual.far, expected.far, error) &&
                isCloseTo(actual.end, expected.end, error) &&
                isCloseTo(actual.weight, expected.weight, error)

fun closeTo(expected: ConicSection, precision: Double = 1.0e-9): TypeSafeMatcher<ConicSection> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

fun ConicSection.shouldEqualToConicSection(expected: ConicSection, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}