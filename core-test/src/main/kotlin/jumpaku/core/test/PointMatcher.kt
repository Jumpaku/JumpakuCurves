package jumpaku.core.test

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.describedBy
import jumpaku.core.affine.Point
import org.apache.commons.math3.util.Precision

fun point(expected: Point, eps: Double = 1.0e-10): Matcher<Point> = Matcher("point matcher") { actual: Point ->
    Precision.equals(actual.x, expected.x, eps) &&
            Precision.equals(actual.y, expected.y, eps) &&
            Precision.equals(actual.z, expected.z, eps) &&
            Precision.equals(actual.r, expected.r, eps)
}.describedBy { "matches $expected" }
