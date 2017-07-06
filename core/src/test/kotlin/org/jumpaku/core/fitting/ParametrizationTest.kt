package org.jumpaku.core.fitting

import io.vavr.API.Array
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.paramPointAssertThat
import org.junit.Test



class ParametrizationTest {

    @Test
    fun testTransformParams() {
        println("TransformParams")
        val data = chordalParametrize(Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        val t = transformParams(data, Interval(2.0, 5.0))
        assertThat(t.size()).isEqualTo(3)
        paramPointAssertThat(t[0]).isEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 2.0))
        paramPointAssertThat(t[1]).isEqualToParamPoint(ParamPoint(Point.xyr( 2.0,-2.0, 2.0), 11/3.0))
        paramPointAssertThat(t[2]).isEqualToParamPoint(ParamPoint(Point.xyr( 2.0, 2.0, 1.0), 5.0))
    }

    @Test
    fun testChordalParametrize() {
        println("ChordalParametrize")
        val data = chordalParametrize(Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        assertThat(data.size()).isEqualTo(3)
        paramPointAssertThat(data[0]).isEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        paramPointAssertThat(data[1]).isEqualToParamPoint(ParamPoint(Point.xyr( 2.0,-2.0, 2.0), 5.0))
        paramPointAssertThat(data[2]).isEqualToParamPoint(ParamPoint(Point.xyr( 2.0, 2.0, 1.0), 9.0))
    }

    @Test
    fun testUniformParametrize() {
        println("UniformParametrize")
        val data = uniformParametrize(Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)))
        assertThat(data.size()).isEqualTo(3)
        paramPointAssertThat(data[0]).isEqualToParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 0.0))
        paramPointAssertThat(data[1]).isEqualToParamPoint(ParamPoint(Point.xyr( 2.0,-2.0, 2.0), 1.0))
        paramPointAssertThat(data[2]).isEqualToParamPoint(ParamPoint(Point.xyr( 2.0, 2.0, 1.0), 2.0))
    }
}

