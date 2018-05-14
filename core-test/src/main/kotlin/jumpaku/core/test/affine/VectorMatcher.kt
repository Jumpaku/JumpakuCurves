package jumpaku.core.test.affine

import jumpaku.core.affine.Vector
import jumpaku.core.test.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: Vector, expected: Vector, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.x, expected.x, error) &&
                isCloseTo(actual.y, expected.y, error) &&
                isCloseTo(actual.z, expected.z, error)

fun Vector.shouldEqualToVector(expected: Vector, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}