package jumpaku.core.test.affine

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.should.shouldMatch
import jumpaku.core.affine.Point
import jumpaku.core.test.point
import org.junit.Test

class PointTest {

    @Test
    fun testProperties() {
        println("Properties")

        //assert.that(Point.x(1.0), point(Point.x(2.0)))
        Point.x(1.0) shouldMatch point(Point.x(2.0))
    }
}