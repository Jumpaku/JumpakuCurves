package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.line
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Test

class LineTest {

    @Test
    fun testLine() {
        println("Line")
        assertThat(line(Point.x(3.0), Point.x(3.0)).error().orNull()!!,
                `is`(instanceOf(IllegalArgumentException::class.java)))
        assertThat(line(Point.x(3.0), Point.x(4.0)).value().isDefined, `is`(true))
    }
}