package jumpaku.core.test.util

import jumpaku.core.util.isEven
import jumpaku.core.util.isOdd
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

class IntegersKtTest {

    @Test
    fun testIsOdd() {
        println("IsOdd")
        (-2).isOdd().shouldBeFalse()
        (-1).isOdd().shouldBeTrue()
        (0).isOdd().shouldBeFalse()
        (1).isOdd().shouldBeTrue()
        (2).isOdd().shouldBeFalse()
    }

    @Test
    fun testIsEven() {
        println("IsEven")
        (-2).isEven().shouldBeTrue()
        (-1).isEven().shouldBeFalse()
        (0).isEven().shouldBeTrue()
        (1).isEven().shouldBeFalse()
        (2).isEven().shouldBeTrue()
    }

}