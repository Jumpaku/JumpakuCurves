package jumpaku.curves.core.test

import org.amshove.kluent.should
import org.apache.commons.math3.util.Precision
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Matchers


fun <A> matcher(message: String, test: (A) -> Boolean): TypeSafeMatcher<A> = object : TypeSafeMatcher<A>() {

    override fun describeTo(description: Description) { description.appendText(message) }

    override fun matchesSafely(actual: A): Boolean = test(actual)
}

fun isCloseTo(actual: Double, expected: Double, error: Double = 1.0e-9): Boolean = Precision.equals(actual, expected, error)

fun closeTo(expected: Double, precision: Double = 1.0e-9): Matcher<Double> = Matchers.closeTo(expected, precision)

fun <A> eachItemMatchesWith(list: List<A>, test: (A, A) -> Boolean): TypeSafeMatcher<List<A>> =
        matcher("each item matches with <$list>") { actual ->
            actual.size == list.size && actual.zip(list).all { (a, e) -> test(a, e) }
        }

fun Double.shouldBeCloseTo(expected: Double, error: Double = 1.0e-9) = this.should("$this should be close to $expected with precision $error") {
    isCloseTo(this, expected, error)
}
