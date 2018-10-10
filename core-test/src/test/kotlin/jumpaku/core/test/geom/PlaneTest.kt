package jumpaku.core.test.geom

import jumpaku.core.geom.Point
import jumpaku.core.geom.plane
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

class PlaneTest {

    @Test
    fun testPlane() {
        println("Plane")
        plane(Point.x(3.0), Point.x(3.0), Point.x(3.0)).error().orNull()!!
                .shouldBeInstanceOf(IllegalArgumentException::class.java)
        plane(Point.x(3.0), Point.x(3.0), Point.x(4.0)).error().orNull()!!
                .shouldBeInstanceOf(IllegalArgumentException::class.java)
        plane(Point.x(3.0), Point.x(3.0), Point.xy(0.0, 3.0)).error().orNull()!!
                .shouldBeInstanceOf(IllegalArgumentException::class.java)
        plane(Point.x(3.0), Point.x(4.0), Point.xy(0.0, 3.0)).value().isDefined
                .shouldBeTrue()
    }
}