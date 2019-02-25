package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.line
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

class LineTest {

    @Test
    fun testLine() {
        println("Line")
        line(Point.x(3.0), Point.x(3.0)).error().orNull()!!.shouldBeInstanceOf(IllegalArgumentException::class.java)
        line(Point.x(3.0), Point.x(4.0)).value().isDefined.shouldBeTrue()
    }
}