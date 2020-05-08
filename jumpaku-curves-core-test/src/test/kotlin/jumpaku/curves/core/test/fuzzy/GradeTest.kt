package jumpaku.curves.core.test.fuzzy

import jumpaku.commons.control.result
import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.fuzzy.Grade
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test

class GradeTest {

    @Test
    fun testConstructorException() {
        println("ConstructorException")
        val a0 = result { Grade(-0.5) }.error().orThrow()
        val a1 = result { Grade(1.5) }.error().orThrow()
        val a2 = result { Grade(Double.NaN) }.error().orThrow()
        val a3 = result { Grade(Double.NEGATIVE_INFINITY) }.error().orThrow()
        val a4 = result { Grade(Double.POSITIVE_INFINITY) }.error().orThrow()
        val e = IllegalArgumentException::class.java
        assertThat(a0, `is`(instanceOf(e)))
        assertThat(a1, `is`(instanceOf(e)))
        assertThat(a2, `is`(instanceOf(e)))
        assertThat(a3, `is`(instanceOf(e)))
        assertThat(a4, `is`(instanceOf(e)))
    }

    @Test
    fun testToGrade() {
        println("ToGrade")
        assertThat(Grade(true).value, `is`(closeTo(1.0)))
        assertThat(Grade(false).value, `is`(closeTo(0.0)))
    }

    @Test
    fun testToBoolean() {
        println("ToBoolean")
        assertThat(Grade(0.0).toBoolean(0.5, true), `is`(false))
        assertThat(Grade(0.5).toBoolean(0.5, true), `is`(true))
        assertThat(Grade(0.5).toBoolean(0.5, false), `is`(false))
        assertThat(Grade(1.0).toBoolean(0.5, true), `is`(true))
    }

    @Test
    fun testCompareTo() {
        println("CompareTo")
        assertThat(Grade(0.3).compareTo(Grade(0.8)), `is`(lessThan(0)))
        assertThat(Grade(0.9).compareTo(Grade(0.8)), `is`(greaterThan(0)))
        assertThat(Grade(0.8).compareTo(Grade(0.8)), `is`(equalTo(0)))
        assertThat((Grade(0.3) < Grade(0.8)), `is`(true))
        assertThat((Grade(0.3) <= Grade(0.8)), `is`(true))
        assertThat((Grade(0.9) > Grade(0.8)), `is`(true))
        assertThat((Grade(0.9) >= Grade(0.8)), `is`(true))
        assertThat((Grade(0.8) <= Grade(0.8)), `is`(true))
        assertThat((Grade(0.8) >= Grade(0.8)), `is`(true))
    }

    @Test
    fun testAnd() {
        println("And")
        assertThat(Grade(0.3).and(Grade(0.8)).value, `is`(closeTo(0.3)))
        assertThat(Grade(0.9).and(Grade(0.8)).value, `is`(closeTo(0.8)))
        assertThat(Grade(0.8).and(Grade(0.8)).value, `is`(closeTo(0.8)))
        assertThat((Grade(0.3) and Grade(0.8)).value, `is`(closeTo(0.3)))
        assertThat((Grade(0.9) and Grade(0.8)).value, `is`(closeTo(0.8)))
        assertThat((Grade(0.8) and Grade(0.8)).value, `is`(closeTo(0.8)))
    }

    @Test
    fun testOr() {
        println("Or")
        assertThat(Grade(0.3).or(Grade(0.8)).value, `is`(closeTo(0.8)))
        assertThat(Grade(0.9).or(Grade(0.8)).value, `is`(closeTo(0.9)))
        assertThat(Grade(0.8).or(Grade(0.8)).value, `is`(closeTo(0.8)))
        assertThat((Grade(0.3) or Grade(0.8)).value, `is`(closeTo(0.8)))
        assertThat((Grade(0.9) or Grade(0.8)).value, `is`(closeTo(0.9)))
        assertThat((Grade(0.8) or Grade(0.8)).value, `is`(closeTo(0.8)))
    }

    @Test
    fun testNot() {
        println("Not")
        assertThat(Grade(0.3).not().value, `is`(closeTo(0.7)))
        assertThat(Grade(0.9).not().value, `is`(closeTo(0.1)))
        assertThat(Grade(0.8).not().value, `is`(closeTo(0.2)))
        assertThat((!Grade(0.3)).value, `is`(closeTo(0.7)))
        assertThat((!Grade(0.9)).value, `is`(closeTo(0.1)))
        assertThat((!Grade(0.8)).value, `is`(closeTo(0.2)))
    }

    @Test
    fun testGetValue() {
        println("GetValue")
        assertThat(Grade(1.0).value, `is`(closeTo(1.0)))
        assertThat(Grade(0.0).value, `is`(closeTo(0.0)))
        assertThat(Grade(0.5).value, `is`(closeTo(0.5)))
    }

    @Test
    fun testClamp() {
        println("Clamp")
        assertThat(Grade.clamped(1.5).value, `is`(closeTo(1.0)))
        assertThat(Grade.clamped(1.0).value, `is`(closeTo(1.0)))
        assertThat(Grade.clamped(0.5).value, `is`(closeTo(0.5)))
        assertThat(Grade.clamped(0.0).value, `is`(closeTo(0.0)))
        assertThat(Grade.clamped(-0.5).value, `is`(closeTo(0.0)))
    }
}

