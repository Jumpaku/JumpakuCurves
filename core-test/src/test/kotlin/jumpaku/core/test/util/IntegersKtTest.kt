package jumpaku.core.test.util

import jumpaku.core.util.isEven
import jumpaku.core.util.isOdd
import org.assertj.core.api.Assertions.*
import org.junit.Test

class IntegersKtTest {

    @Test
    fun testIsOdd() {
        println("IsOdd")
        assertThat((-2).isOdd()).isFalse()
        assertThat((-1).isOdd()).isTrue()
        assertThat((0).isOdd()).isFalse()
        assertThat((1).isOdd()).isTrue()
        assertThat((2).isOdd()).isFalse()
    }

    @Test
    fun testIsEven() {
        println("IsEven")
        assertThat((-2).isEven()).isTrue()
        assertThat((-1).isEven()).isFalse()
        assertThat((0).isEven()).isTrue()
        assertThat((1).isEven()).isFalse()
        assertThat((2).isEven()).isTrue()
    }

}