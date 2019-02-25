package jumpaku.curves.core.test.curve.polyline

import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.core.test.matcher
import org.amshove.kluent.should
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Polyline, expected: Polyline, error: Double = 1.0e-9): Boolean =
        (actual.points.size == expected.points.size) &&
                actual.points.zip(expected.points) { a, e -> isCloseTo(a, e, error) }.all { it }

fun closeTo(expected: Polyline, precision: Double = 1.0e-9): TypeSafeMatcher<Polyline> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

fun Polyline.shouldEqualToPolyline(expected: Polyline, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
