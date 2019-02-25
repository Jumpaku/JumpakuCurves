package jumpaku.core.test.curve

import jumpaku.core.curve.*
import jumpaku.core.geom.Point
import org.amshove.kluent.shouldEqualTo
import org.junit.Test
import kotlin.math.sqrt

class ParametrizationTest {

    @Test
    fun testTransformParams() {
        println("TransformParams")
        val data = chordalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
                .orThrow()
        val t = transformParams(data, range = Interval(2.0, 5.0))
        t.size.shouldEqualTo(3)
        t[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 2.0))
        t[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 11 / 3.0))
        t[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 5.0))
    }

    @Test
    fun testChordalParametrize() {
        println("ChordalParametrize")
        val data = chordalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
                .orThrow()
        data.size.shouldEqualTo(3)
        data[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        data[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 5/9.0))
        data[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 1.0))
    }

    @Test
    fun testCentripetalParametrize() {
        println("CentripetalParametrize")
        val data = centripetalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
                .orThrow()
        data.size.shouldEqualTo(3)
        data[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        data[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), sqrt(5.0) / (sqrt(5.0) + 2)))
        data[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 1.0))
    }

    @Test
    fun testUniformParametrize() {
        println("UniformParametrize")
        val data = uniformParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        data.size.shouldEqualTo(3)
        data[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        data[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 0.5))
        data[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 1.0))
    }
}

