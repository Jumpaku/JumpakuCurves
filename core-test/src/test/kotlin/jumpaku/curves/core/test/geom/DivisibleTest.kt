
package jumpaku.curves.core.test.geom


import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.test.shouldBeCloseTo
import org.junit.Test

class DivisibleTest {

    @Test
    fun testDivide() {
        println("Divide")
        val c0 = 1.0
        val c1 = 2.0
        c0.lerp( 0.3, c1).shouldBeCloseTo(1.3)
        c0.lerp(-1.0, c1).shouldBeCloseTo(0.0)
        c0.lerp( 2.0, c1).shouldBeCloseTo(3.0)
        c0.lerp( 0.0, c1).shouldBeCloseTo(1.0)
        c0.lerp( 1.0, c1).shouldBeCloseTo(2.0)
    }
}