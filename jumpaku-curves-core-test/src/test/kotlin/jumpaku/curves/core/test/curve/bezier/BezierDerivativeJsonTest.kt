package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bezier.BezierDerivative
import jumpaku.curves.core.curve.bezier.BezierDerivativeJson
import jumpaku.curves.core.geom.Vector
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class BezierDerivativeJsonTest {

    @Test
    fun testBezierDerivativeJson() {
        println("BezierDerivativeJson")
        val p = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        val a = BezierDerivativeJson.toJsonStr(p).parseJson().let { BezierDerivativeJson.fromJson(it) }.toBezier()
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(p.toBezier())))
    }

}