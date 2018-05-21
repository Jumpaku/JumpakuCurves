package jumpaku.core.test.affine

import io.vavr.API
import jumpaku.core.geom.ParamPoint
import jumpaku.core.geom.Point
import jumpaku.core.curve.Interval
import jumpaku.core.geom.chordalParametrize
import jumpaku.core.geom.transformParams
import jumpaku.core.geom.uniformParametrize
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class ParametrizationTest {

    @Test
    fun testTransformParams() {
        println("TransformParams")
        val data = chordalParametrize(API.Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        val t = transformParams(data, Interval(2.0, 5.0)).get()
        t.size().shouldEqualTo(3)
        t[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 2.0))
        t[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 11 / 3.0))
        t[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 5.0))
    }

    @Test
    fun testChordalParametrize() {
        println("ChordalParametrize")
        val data = chordalParametrize(API.Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        data.size().shouldEqualTo(3)
        data[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        data[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 5.0))
        data[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 9.0))
    }

    @Test
    fun testUniformParametrize() {
        println("UniformParametrize")
        val data = uniformParametrize(API.Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        data.size().shouldEqualTo(3)
        data[0].shouldEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        data[1].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, -2.0, 2.0), 1.0))
        data[2].shouldEqualToParamPoint(ParamPoint(Point.xyr(2.0, 2.0, 1.0), 2.0))
    }
}

