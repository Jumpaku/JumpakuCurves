package jumpaku.curves.core.test.curve

import jumpaku.commons.json.parseJson
import jumpaku.commons.test.math.closeTo
import jumpaku.curves.core.curve.Interval
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class IntervalTest {

    val i = Interval(-2.3, 3.4)

    @Test
    fun testConstructorException() {
        println("ConstructorException")
        assertThrows<IllegalArgumentException> { Interval(4.0, -3.0) }
    }

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(i.span, `is`(closeTo(5.7)))
    }

    @Test
    fun testSample() {
        println("Sample")
        val i0 = Interval(-0.1, 0.5).sample(7)
        assertThat(i0.size, `is`(7))
        assertThat(i0[0], `is`(closeTo(-0.1)))
        assertThat(i0[1], `is`(closeTo(0.0)))
        assertThat(i0[2], `is`(closeTo(0.1)))
        assertThat(i0[3], `is`(closeTo(0.2)))
        assertThat(i0[4], `is`(closeTo(0.3)))
        assertThat(i0[5], `is`(closeTo(0.4)))
        assertThat(i0[6], `is`(closeTo(0.5)))
        val i1 = Interval(-0.1, 0.5).sample(0.11)
        assertThat(i1.size, `is`(7))
        assertThat(i1[0], `is`(closeTo(-0.1)))
        assertThat(i1[1], `is`(closeTo(0.0)))
        assertThat(i1[2], `is`(closeTo(0.1)))
        assertThat(i1[3], `is`(closeTo(0.2)))
        assertThat(i1[4], `is`(closeTo(0.3)))
        assertThat(i1[5], `is`(closeTo(0.4)))
        assertThat(i1[6], `is`(closeTo(0.5)))
    }

    @Test
    fun testContains() {
        println("Contains")
        assertThat((-2.3 in i), `is`(true))
        assertThat((3.4 in i), `is`(true))
        assertThat((1.0 in i), `is`(true))
        assertThat((-3.0 in i), `is`(false))
        assertThat((4.3 in i), `is`(false))
        assertThat((i in i), `is`(true))
        assertThat((Interval(-3.0, 3.0) in i), `is`(false))
        assertThat((Interval(2.0, 4.0) in i), `is`(false))
        assertThat((Interval(-2.0, 3.0) in i), `is`(true))
        assertThat((Interval(-3.0, 4.0) in i), `is`(false))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = i.toString().parseJson().tryMap { Interval.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(i)))
    }
}