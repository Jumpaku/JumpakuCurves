package jumpaku.core.test.util

import jumpaku.core.test.shouldBeCloseTo
import jumpaku.core.util.sum
import org.junit.Test

class DoublesTest {

    @Test
    fun testSum() {
        println("Sum")
        val doubles = doubleArrayOf(
                1.0,
                1e-9, 1e-9, 1e-9, 1e-9, 1e-9,
                1e-9, 1e-9, 1e-9, 1e-9, 1e-9,
                -1.0,
                -1e-9, -1e-9, -1e-9, -1e-9, -1e-9,
                -1e-9, -1e-9, -1e-9, -1e-9, -1e-9,
                1.0
        )
        val a = sum(doubles)
        a.shouldBeCloseTo(1.0, 0.0)

        val b = sum(doubles.toList())
        b.shouldBeCloseTo(1.0, 0.0)

    }
}