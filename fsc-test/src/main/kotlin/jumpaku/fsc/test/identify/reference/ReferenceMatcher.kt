package jumpaku.fsc.test.identify.reference

import jumpaku.core.test.curve.isCloseTo
import jumpaku.core.test.curve.rationalbezier.isCloseTo
import jumpaku.fsc.identify.reference.Reference
import org.amshove.kluent.should

fun isCloseTo(actual: Reference, expected: Reference, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.base, expected.base, error)
        && isCloseTo(actual.domain, expected.domain, error)

fun Reference.shouldEqualToReference(expected: Reference, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
