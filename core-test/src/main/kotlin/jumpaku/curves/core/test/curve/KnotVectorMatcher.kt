package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.test.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: Knot, expected: Knot, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.value, expected.value, error) && actual.multiplicity == expected.multiplicity

fun isCloseTo(actual: KnotVector, expected: KnotVector, error: Double = 1.0e-9): Boolean =
        (actual.knots.size == expected.knots.size) &&
                actual.knots.zip(expected.knots) { a, e -> isCloseTo(a, e, error) }.all { it } &&
                actual.degree == expected.degree &&
                isCloseTo(actual.domain, expected.domain, error)

fun KnotVector.shouldEqualToKnotVector(expected: KnotVector, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
