package jumpaku.core.curve

import jumpaku.core.json.parseJson
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.junit.Test

fun intervalAssertThat(actual: Interval): IntervalAssert = IntervalAssert(actual)

class IntervalAssert(actual: Interval) : AbstractAssert<IntervalAssert, Interval>(actual, IntervalAssert::class.java) {
    fun isEqualToInterval(expected: Interval, eps: Double = 1.0e-10): IntervalAssert {
        isNotNull

        Assertions.assertThat(actual.begin).`as`("begin of interval")
                .isEqualTo(expected.begin, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.end).`as`("end of interval")
                .isEqualTo(expected.end, Assertions.withPrecision(eps))

        return this
    }
}

class IntervalTest {

    @Test
    fun testConstructorException() {
        println("ConstructorException")
        assertThatIllegalArgumentException().isThrownBy { Interval(4.0, -3.0) }
    }
    @Test
    fun testProperties() {
        println("Properties")
        val b = Interval(-2.3, 3.4).begin
        val e = Interval(-2.3, 3.4).end
        val s = Interval(-2.3, 3.4).span
        assertThat(b).isEqualTo(-2.3, withPrecision(1.0e-10))
        assertThat(e).isEqualTo( 3.4, withPrecision(1.0e-10))
        assertThat(s).isEqualTo( 5.7, withPrecision(1.0e-10))
    }

    @Test
    fun testSample() {
        println("Sample")
        val i0 = Interval(-0.1, 0.5).sample(7)
        assertThat(i0).hasSize(7)
        assertThat(i0[0]).isEqualTo(-0.1, withPrecision(1.0e-10))
        assertThat(i0[1]).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(i0[2]).isEqualTo( 0.1, withPrecision(1.0e-10))
        assertThat(i0[3]).isEqualTo( 0.2, withPrecision(1.0e-10))
        assertThat(i0[4]).isEqualTo( 0.3, withPrecision(1.0e-10))
        assertThat(i0[5]).isEqualTo( 0.4, withPrecision(1.0e-10))
        assertThat(i0[6]).isEqualTo( 0.5, withPrecision(1.0e-10))
        val i1 = Interval(-0.1, 0.5).sample(0.11)
        assertThat(i1).hasSize(7)
        assertThat(i1[0]).isEqualTo(-0.1, withPrecision(1.0e-10))
        assertThat(i1[1]).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(i1[2]).isEqualTo( 0.1, withPrecision(1.0e-10))
        assertThat(i1[3]).isEqualTo( 0.2, withPrecision(1.0e-10))
        assertThat(i1[4]).isEqualTo( 0.3, withPrecision(1.0e-10))
        assertThat(i1[5]).isEqualTo( 0.4, withPrecision(1.0e-10))
        assertThat(i1[6]).isEqualTo( 0.5, withPrecision(1.0e-10))
    }

    @Test
    fun testContains() {
        println("Contains")
        val t0 = Interval(-2.3, 3.4).contains(-2.3)
        val t1 = Interval(-2.3, 3.4).contains( 3.4)
        val t2 = Interval(-2.3, 3.4).contains( 1.0)
        val t3 = Interval(-2.3, 3.4).contains(-3.0)
        val t4 = Interval(-2.3, 3.4).contains( 4.3)
        assertThat(t0).isTrue()
        assertThat(t1).isTrue()
        assertThat(t2).isTrue()
        assertThat(t3).isFalse()
        assertThat(t4).isFalse()
        val i0 = Interval(-2.3, 3.4).contains(Interval(-2.3, 3.4))
        val i1 = Interval(-2.3, 3.4).contains(Interval(-3.0, 3.0))
        val i2 = Interval(-2.3, 3.4).contains(Interval(2.0, 4.0))
        val i3 = Interval(-2.3, 3.4).contains(Interval(-2.0, 3.0))
        val i4 = Interval(-2.3, 3.4).contains(Interval(-3.0, 4.0))
        assertThat(i0).isTrue()
        assertThat(i1).isFalse()
        assertThat(i2).isFalse()
        assertThat(i3).isTrue()
        assertThat(i4).isFalse()
    }

    @Test
    fun testToString() {
        println("ToString")
        val i = Interval(-2.3, 3.4)
        intervalAssertThat(i.toString().parseJson().get().interval).isEqualTo(i)
    }
}