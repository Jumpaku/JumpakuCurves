package jumpaku.core.test.affine

import jumpaku.core.affine.divide
import org.assertj.core.api.Assertions.*
import org.junit.Test

/**
 * Created by jumpaku on 2017/06/09.
 */
class DivisibleTest {

    @Test
    fun testDivide() {
        println("Divide")
        val c0 = 1.0
        val c1 = 2.0
        assertThat(c0.divide( 0.3, c1)).isEqualTo(1.3, withPrecision(1.0e-10))
        assertThat(c0.divide(-1.0, c1)).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(c0.divide( 2.0, c1)).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(c0.divide( 0.0, c1)).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(c0.divide( 1.0, c1)).isEqualTo(2.0, withPrecision(1.0e-10))
    }
}