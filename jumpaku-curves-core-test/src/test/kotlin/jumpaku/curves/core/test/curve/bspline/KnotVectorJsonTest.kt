package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.curve.bspline.KnotVectorJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class KnotVectorJsonTest {

    val k = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)

    @Test
    fun testKnotVectorJson() {
        println("KnotVectorJson")
        val l = KnotVectorJson.toJsonStr(k).parseJson().let { KnotVectorJson.fromJson(it) }
        assertThat(l, `is`(closeTo(k)))
    }

}