package org.jumpaku.core.fit

import com.github.salomonbrys.kotson.fromJson
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.paramPointAssertThat
import org.jumpaku.core.json.prettyGson
import org.junit.Test

class WeightedParamPointTest {

    @Test
    fun testProperties() {
        println("Properties")
        val w = ParamPoint(Point.xr(1.0, 10.0), 1.0).weighted(2.0)
        paramPointAssertThat(w.paramPoint).isEqualToParamPoint(ParamPoint(Point.xr(1.0, 10.0), 1.0))
        assertThat(w.weight).isEqualTo(2.0, withPrecision(1.0e-10))
        pointAssertThat(w.point).isEqualTo(Point.xr(1.0, 10.0))
        assertThat(w.param).isEqualTo(1.0, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val w = ParamPoint(Point.xr(1.0, 10.0), 1.0).weighted(2.0)
        weightedWeightedParamPointAssertThat(prettyGson.fromJson<WeightedParamPointJson>(w.toString()).weightedParamPoint()).isEqualToWeightedParamPoint(w)
    }
}