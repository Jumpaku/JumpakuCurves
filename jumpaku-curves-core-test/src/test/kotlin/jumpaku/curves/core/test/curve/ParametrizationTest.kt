package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.*
import jumpaku.curves.core.geom.Point
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class ParametrizationTest {

    @Test
    fun testTransformParams() {
        println("TransformParams")
        val data = chordalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
                .orThrow()
        val t = transformParams(data, range = Interval(2.0, 5.0))
        assertThat(t.size, `is`(3))
        assertThat(t[0], `is`(closeTo(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 2.0))))
        assertThat(t[1], `is`(closeTo(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 11 / 3.0))))
        assertThat(t[2], `is`(closeTo(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 5.0))))
    }

    @Test
    fun testChordalParametrize() {
        println("ChordalParametrize")
        val data = chordalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
                .orThrow()
        assertThat(data.size, `is`(3))
        assertThat(data[0], `is`(closeTo(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))))
        assertThat(data[1], `is`(closeTo(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 5 / 9.0))))
        assertThat(data[2], `is`(closeTo(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 1.0))))
    }

    @Test
    fun testCentripetalParametrize() {
        println("CentripetalParametrize")
        val data = centripetalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
                .orThrow()
        assertThat(data.size, `is`(3))
        assertThat(data[0], `is`(closeTo(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))))
        assertThat(data[1], `is`(closeTo(ParamPoint(Point.xyr(2.0, -2.0, 2.0), sqrt(5.0) / (sqrt(5.0) + 2)))))
        assertThat(data[2], `is`(closeTo(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 1.0))))
    }

    @Test
    fun testUniformParametrize() {
        println("UniformParametrize")
        val data = uniformParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        assertThat(data.size, `is`(3))
        assertThat(data[0], `is`(closeTo(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))))
        assertThat(data[1], `is`(closeTo(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 0.5))))
        assertThat(data[2], `is`(closeTo(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 1.0))))
    }
}

