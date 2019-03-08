
package jumpaku.curves.core.test.geom


import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.test.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class LerpableTest {

    @Test
    fun testDivide() {
        println("Divide")
        val c0 = 1.0
        val c1 = 2.0
        assertThat(c0.lerp( 0.3, c1), `is`(closeTo(1.3)))
        assertThat(c0.lerp(-1.0, c1), `is`(closeTo(0.0)))
        assertThat(c0.lerp( 2.0, c1), `is`(closeTo(3.0)))
        assertThat(c0.lerp( 0.0, c1), `is`(closeTo(1.0)))
        assertThat(c0.lerp( 1.0, c1), `is`(closeTo(2.0)))
    }
}