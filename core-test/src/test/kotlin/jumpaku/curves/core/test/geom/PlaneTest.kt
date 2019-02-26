package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.plane
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Test

class PlaneTest {

    @Test
    fun testPlane() {
        println("Plane")
        assertThat(plane(Point.x(3.0), Point.x(3.0), Point.x(3.0)).error().orNull()!!, `is`(instanceOf(IllegalArgumentException::class.java)))
        assertThat(plane(Point.x(3.0), Point.x(3.0), Point.x(4.0)).error().orNull()!!, `is`(instanceOf(IllegalArgumentException::class.java)))
        assertThat(plane(Point.x(3.0), Point.x(3.0), Point.xy(0.0, 3.0)).error().orNull()!!, `is`(instanceOf(IllegalArgumentException::class.java)))
        assertThat(plane(Point.x(3.0), Point.x(4.0), Point.xy(0.0, 3.0)).value().isDefined, `is`(true))
    }
}