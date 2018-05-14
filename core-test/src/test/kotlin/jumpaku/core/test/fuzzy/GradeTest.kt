package jumpaku.core.test.fuzzy

import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.parseJson
import jumpaku.core.test.shouldBeCloseTo
import org.amshove.kluent.*
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class GradeTest {

    @Test
    fun testConstructorException(){
        println("ConstructorException")
        assertThrows<IllegalArgumentException> { Grade(-0.5) }
        assertThrows<IllegalArgumentException> { Grade(1.5) }
        assertThrows<IllegalArgumentException> { Grade(2) }
        assertThrows<IllegalArgumentException> { Grade(-1) }
        assertThrows<IllegalArgumentException> { Grade(Double.NaN) }
        assertThrows<IllegalArgumentException> { Grade(Double.NEGATIVE_INFINITY) }
        assertThrows<IllegalArgumentException> { Grade(Double.POSITIVE_INFINITY) }
    }

    @Test
    fun testToString() {
        println("ToString")
        Grade(1.0).toString().parseJson().flatMap { Grade.fromJson(it.asJsonPrimitive) }.get().value.shouldBeCloseTo(Grade(1.0).value)
        Grade(0.0).toString().parseJson().flatMap { Grade.fromJson(it.asJsonPrimitive) }.get().value.shouldBeCloseTo(Grade(0.0).value)
        Grade(0.5).toString().parseJson().flatMap { Grade.fromJson(it.asJsonPrimitive) }.get().value.shouldBeCloseTo(Grade(0.5).value)
    }

    @Test
    fun testCompareTo() {
        println("CompareTo")
        Grade(0.3).compareTo(Grade(0.8)).shouldBeNegative()
        Grade(0.9).compareTo(Grade(0.8)).shouldBePositive()
        Grade(0.8).compareTo(Grade(0.8)).shouldEqualTo(0)
        (Grade(0.3) < Grade(0.8)).shouldBeTrue()
        (Grade(0.3) <= Grade(0.8)).shouldBeTrue()
        (Grade(0.9) > Grade(0.8)).shouldBeTrue()
        (Grade(0.9) >= Grade(0.8)).shouldBeTrue()
        (Grade(0.8) <= Grade(0.8)).shouldBeTrue()
        (Grade(0.8) >= Grade(0.8)).shouldBeTrue()
    }

    @Test
    fun testAnd() {
        println("And")
        Grade(0.3).and(Grade(0.8)).value.shouldBeCloseTo(0.3)
        Grade(0.9).and(Grade(0.8)).value.shouldBeCloseTo(0.8)
        Grade(0.8).and(Grade(0.8)).value.shouldBeCloseTo(0.8)
        (Grade(0.3) and Grade(0.8)).value.shouldBeCloseTo(0.3)
        (Grade(0.9) and Grade(0.8)).value.shouldBeCloseTo(0.8)
        (Grade(0.8) and Grade(0.8)).value.shouldBeCloseTo(0.8)
    }

    @Test
    fun testOr() {
        println("Or")
        Grade(0.3).or(Grade(0.8)).value.shouldBeCloseTo(0.8)
        Grade(0.9).or(Grade(0.8)).value.shouldBeCloseTo(0.9)
        Grade(0.8).or(Grade(0.8)).value.shouldBeCloseTo(0.8)
        (Grade(0.3) or Grade(0.8)).value.shouldBeCloseTo(0.8)
        (Grade(0.9) or Grade(0.8)).value.shouldBeCloseTo(0.9)
        (Grade(0.8) or Grade(0.8)).value.shouldBeCloseTo(0.8)
    }

    @Test
    fun testNot() {
        println("Not")
        Grade(0.3).not().value.shouldBeCloseTo(0.7)
        Grade(0.9).not().value.shouldBeCloseTo(0.1)
        Grade(0.8).not().value.shouldBeCloseTo(0.2)
        (!Grade(0.3)).value.shouldBeCloseTo(0.7)
        (!Grade(0.9)).value.shouldBeCloseTo(0.1)
        (!Grade(0.8)).value.shouldBeCloseTo(0.2)
    }

    @Test
    fun testGetValue() {
        println("GetValue")
        Grade(1.0).value.shouldBeCloseTo(1.0)
        Grade(0.0).value.shouldBeCloseTo(0.0)
        Grade(0.5).value.shouldBeCloseTo(0.5)
    }

    @Test
    fun testClamp() {
        println("Clamp")
        Grade.clamped(1.5).value.shouldBeCloseTo(1.0)
        Grade.clamped(1.0).value.shouldBeCloseTo(1.0)
        Grade.clamped(0.5).value.shouldBeCloseTo(0.5)
        Grade.clamped(0.0).value.shouldBeCloseTo(0.0)
        Grade.clamped(-0.5).value.shouldBeCloseTo(0.0)
    }
}
