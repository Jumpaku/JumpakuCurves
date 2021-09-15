package jumpaku.curves.core.test.transform

import jumpaku.commons.test.matcher
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.core.transform.AffineTransform
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: AffineTransform, expected: AffineTransform, error: Double): Boolean {
    val p0 = Point.origin
    val p1 = Point(Vector.I)
    val p2 = Point(Vector.J)
    val p3 = Point(Vector.K)
    return isCloseTo(actual(p0), expected(p0), error) &&
            isCloseTo(actual(p1), expected(p1), error) &&
            isCloseTo(actual(p2), expected(p2), error) &&
            isCloseTo(actual(p3), expected(p3), error)
}

fun closeTo(expected: AffineTransform, precision: Double = 1.0e-9): TypeSafeMatcher<AffineTransform> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }