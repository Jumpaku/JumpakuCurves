package jumpaku.curves.core.test.util

import jumpaku.curves.core.util.isEven
import jumpaku.curves.core.util.isOdd
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class IntegersKtTest {

    @Test
    fun testIsOdd() {
        println("IsOdd")
        assertThat((-2).isOdd(), `is`(false))
        assertThat((-1).isOdd(), `is`(true))
        assertThat((0).isOdd(), `is`(false))
        assertThat((1).isOdd(), `is`(true))
        assertThat((2).isOdd(), `is`(false))
    }

    @Test
    fun testIsEven() {
        println("IsEven")
        assertThat((-2).isEven(), `is`(true))
        assertThat((-1).isEven(), `is`(false))
        assertThat((0).isEven(), `is`(true))
        assertThat((1).isEven(), `is`(false))
        assertThat((2).isEven(), `is`(true))
    }

}