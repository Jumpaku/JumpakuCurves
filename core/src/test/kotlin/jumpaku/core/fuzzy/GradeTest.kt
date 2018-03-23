package jumpaku.core.fuzzy

import org.assertj.core.api.Assertions.*
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/09.
 */
class GradeTest {

    @Test
    fun testConstructorException(){
        println("ConstructorException")
        assertThatIllegalArgumentException().isThrownBy { Grade(-0.5) }
        assertThatIllegalArgumentException().isThrownBy { Grade(1.5) }
        assertThatIllegalArgumentException().isThrownBy { Grade(2) }
        assertThatIllegalArgumentException().isThrownBy { Grade(-1) }
        assertThatIllegalArgumentException().isThrownBy { Grade(Double.NaN) }
        assertThatIllegalArgumentException().isThrownBy { Grade(Double.NEGATIVE_INFINITY) }
        assertThatIllegalArgumentException().isThrownBy { Grade(Double.POSITIVE_INFINITY) }
    }

    @Test
    fun testToString() {
        println("ToString")
        assertThat(Grade(1.0).toString()).isEqualTo("1.0")
        assertThat(Grade(0.0).toString()).isEqualTo("0.0")
        assertThat(Grade(0.5).toString()).isEqualTo("0.5")
    }

    @Test
    fun testFromJson() {
        println("FromJson")
        assertThat(Grade.fromJsonString("1.0").get().value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(Grade.fromJsonString("0.0").get().value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(Grade.fromJsonString("0.5").get().value).isEqualTo(0.5, withPrecision(1.0e-10))
    }

    @Test
    fun testCompareTo() {
        println("CompareTo")
        assertThat(Grade(0.3).compareTo(Grade(0.8))).isNegative()
        assertThat(Grade(0.9).compareTo(Grade(0.8))).isPositive()
        assertThat(Grade(0.8).compareTo(Grade(0.8))).isZero()
        assertThat(Grade(0.3) < Grade(0.8)).isTrue()
        assertThat(Grade(0.3) <= Grade(0.8)).isTrue()
        assertThat(Grade(0.9) > Grade(0.8)).isTrue()
        assertThat(Grade(0.9) >= Grade(0.8)).isTrue()
        assertThat(Grade(0.8) <= Grade(0.8)).isTrue()
        assertThat(Grade(0.8) >= Grade(0.8)).isTrue()
    }

    @Test
    fun testAnd() {
        println("And")
        assertThat(Grade(0.3).and(Grade(0.8)).value).isEqualTo(0.3, withPrecision(1.0e-10))
        assertThat(Grade(0.9).and(Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
        assertThat(Grade(0.8).and(Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
        assertThat((Grade(0.3) and Grade(0.8)).value).isEqualTo(0.3, withPrecision(1.0e-10))
        assertThat((Grade(0.9) and Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
        assertThat((Grade(0.8) and Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
    }

    @Test
    fun testOr() {
        println("Or")
        assertThat(Grade(0.3).or(Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
        assertThat(Grade(0.9).or(Grade(0.8)).value).isEqualTo(0.9, withPrecision(1.0e-10))
        assertThat(Grade(0.8).or(Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
        assertThat((Grade(0.3) or Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
        assertThat((Grade(0.9) or Grade(0.8)).value).isEqualTo(0.9, withPrecision(1.0e-10))
        assertThat((Grade(0.8) or Grade(0.8)).value).isEqualTo(0.8, withPrecision(1.0e-10))
    }

    @Test
    fun testNot() {
        println("Not")
        assertThat(Grade(0.3).not().value).isEqualTo(0.7, withPrecision(1.0e-10))
        assertThat(Grade(0.9).not().value).isEqualTo(0.1, withPrecision(1.0e-10))
        assertThat(Grade(0.8).not().value).isEqualTo(0.2, withPrecision(1.0e-10))
        assertThat((!Grade(0.3)).value).isEqualTo(0.7, withPrecision(1.0e-10))
        assertThat((!Grade(0.9)).value).isEqualTo(0.1, withPrecision(1.0e-10))
        assertThat((!Grade(0.8)).value).isEqualTo(0.2, withPrecision(1.0e-10))
    }

    @Test
    fun testGetValue() {
        println("GetValue")
        assertThat(Grade(1.0).value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(Grade(0.0).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(Grade(0.5).value).isEqualTo(0.5, withPrecision(1.0e-10))
    }

    @Test
    fun testClamp() {
        println("Clamp")
        assertThat(Grade.clamped(1.5).value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(Grade.clamped(1.0).value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(Grade.clamped(0.5).value).isEqualTo(0.5, withPrecision(1.0e-10))
        assertThat(Grade.clamped(0.0).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(Grade.clamped(-0.5).value).isEqualTo(0.0, withPrecision(1.0e-10))
    }
}
