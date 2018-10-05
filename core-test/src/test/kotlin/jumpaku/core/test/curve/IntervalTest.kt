package jumpaku.core.test.curve

import jumpaku.core.curve.Interval
import jumpaku.core.json.parseJson
import jumpaku.core.test.shouldBeCloseTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
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
        i.span.shouldBeCloseTo( 5.7)
    }

    @Test
    fun testSample() {
        println("Sample")
        val i0 = Interval(-0.1, 0.5).sample(7)
        i0.size().shouldEqual(7)
        i0[0].shouldBeCloseTo(-0.1)
        i0[1].shouldBeCloseTo( 0.0)
        i0[2].shouldBeCloseTo( 0.1)
        i0[3].shouldBeCloseTo( 0.2)
        i0[4].shouldBeCloseTo( 0.3)
        i0[5].shouldBeCloseTo( 0.4)
        i0[6].shouldBeCloseTo( 0.5)
        val i1 = Interval(-0.1, 0.5).sample(0.11)
        i1.size().shouldEqual(7)
        i1[0].shouldBeCloseTo(-0.1)
        i1[1].shouldBeCloseTo( 0.0)
        i1[2].shouldBeCloseTo( 0.1)
        i1[3].shouldBeCloseTo( 0.2)
        i1[4].shouldBeCloseTo( 0.3)
        i1[5].shouldBeCloseTo( 0.4)
        i1[6].shouldBeCloseTo( 0.5)
    }

    @Test
    fun testContains() {
        println("Contains")
        (-2.3 in i).shouldBeTrue()
        (3.4 in i).shouldBeTrue()
        (1.0 in i).shouldBeTrue()
        (-3.0 in i).shouldBeFalse()
        (4.3 in i).shouldBeFalse()
        (i in i).shouldBeTrue()
        (Interval(-3.0, 3.0) in i).shouldBeFalse()
        (Interval(2.0, 4.0) in i).shouldBeFalse()
        (Interval(-2.0, 3.0) in i).shouldBeTrue()
        (Interval(-3.0, 4.0) in i).shouldBeFalse()
    }

    @Test
    fun testToString() {
        println("ToString")
        i.toString().parseJson().tryFlatMap { Interval.fromJson(it) }.orThrow().shouldEqualToInterval(i)
    }
}