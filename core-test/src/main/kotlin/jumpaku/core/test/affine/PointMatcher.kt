package jumpaku.core.test

import jumpaku.core.affine.Point
import org.amshove.kluent.should
import org.apache.commons.math3.util.Precision


fun Point.shouldBe(expected: Point, delta: Double = 1.0e-9) = this.should("$this should be $expected") {
    Precision.equals(x, expected.x, delta) &&
            Precision.equals(y, expected.y, delta) &&
            Precision.equals(z, expected.z, delta) &&
            Precision.equals(r, expected.r, delta)
}
