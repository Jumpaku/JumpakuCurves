package jumpaku.core.test.curve

import io.vavr.API
import jumpaku.core.curve.ParamPoint
import jumpaku.core.geom.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.chordalParametrize
import jumpaku.core.curve.transformParams
import jumpaku.core.curve.uniformParametrize
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class ParametrizationTest {

    @Test
    fun testTransformParams() {
        println("TransformParams")
        val data = chordalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        val t = transformParams(data, Interval(2.0, 5.0)).orThrow()
        t.size.shouldEqualTo(3)
        t[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 2.0))
        t[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 11 / 3.0))
        t[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 5.0))
    }

    @Test
    fun testChordalParametrize() {
        println("ChordalParametrize")
        val data = chordalParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        data.size.shouldEqualTo(3)
        data[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        data[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 5.0))
        data[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 9.0))
    }

    @Test
    fun testUniformParametrize() {
        println("UniformParametrize")
        val data = uniformParametrize(listOf(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        data.size.shouldEqualTo(3)
        data[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        data[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 1.0))
        data[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 2.0))
    }
}

