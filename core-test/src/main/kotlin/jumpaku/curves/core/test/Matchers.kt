package jumpaku.curves.core.test

import org.apache.commons.math3.util.Precision
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher


fun <A> matcher(message: String, test: (A) -> Boolean): TypeSafeMatcher<A> = object : TypeSafeMatcher<A>() {

    override fun describeTo(description: Description) { description.appendText(message) }

    override fun matchesSafely(actual: A): Boolean = test(actual)
}

fun isCloseTo(actual: Double, expected: Double, error: Double = 1.0e-9): Boolean = Precision.equals(actual, expected, error)

fun closeTo(expected: Double, precision: Double = 1.0e-9): Matcher<Double> = Matchers.closeTo(expected, precision)

