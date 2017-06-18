package org.jumpaku.core.fitting

import io.vavr.API.Array
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.Interval
import org.junit.Test



class ParameterizationTest {
    @Test
    fun testChordalParametrize() {
        println("ChordalParametrize")
        val data = chordalParametrize(Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)), Interval(1.0, 10.0))
        assertThat(data.size()).isEqualTo(3)
        paramPointAssertThat(data[0]).isParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 1.0))
        paramPointAssertThat(data[1]).isParamPoint(ParamPoint(Point.xyr( 2.0,-2.0, 2.0), 6.0))
        paramPointAssertThat(data[2]).isParamPoint(ParamPoint(Point.xyr( 2.0, 2.0, 1.0), 10.0))
    }

    @Test
    fun testUniformParametrize() {
        println("UniformParametrize")
        val data = uniformParametrize(Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)), Interval(1.0, 10.0))
        assertThat(data.size()).isEqualTo(3)
        paramPointAssertThat(data[0]).isParamPoint(ParamPoint(Point.xyr(-1.0, 2.0, 3.0), 1.0))
        paramPointAssertThat(data[1]).isParamPoint(ParamPoint(Point.xyr( 2.0,-2.0, 2.0), 5.5))
        paramPointAssertThat(data[2]).isParamPoint(ParamPoint(Point.xyr( 2.0, 2.0, 1.0), 10.0))
    }
}

