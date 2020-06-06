package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.curve.bezier.ConicSectionJson
import jumpaku.curves.core.geom.Point
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class ConicSectionJsonTest {

    private val R2 = FastMath.sqrt(2.0)

    private val cs = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)

    @Test
    fun testConicSectionJson() {
        println("ConicSectionJson")
        val i = cs
        val a = ConicSectionJson.toJsonStr(cs).parseJson().let { ConicSectionJson.fromJson(it) }
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(i)))
    }

}