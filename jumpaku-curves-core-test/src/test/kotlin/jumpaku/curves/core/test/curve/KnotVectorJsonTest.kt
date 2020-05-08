package jumpaku.curves.core.test.curve

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.*
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class KnotJsonTest {


    @Test
    fun testKnotJson() {
        println("KnotJson")
        val e = Knot(3.0, 5)
        val l = KnotJson.toJsonStr(e).parseJson().let { KnotJson.fromJson(it) }
        Assert.assertThat(l.value, CoreMatchers.`is`(jumpaku.commons.math.test.closeTo(e.value)))
        Assert.assertThat(l.multiplicity, CoreMatchers.`is`(e.multiplicity))
    }

}

class KnotVectorJsonTest {

    val k = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)

    @Test
    fun testKnotVectorJson() {
        println("KnotVectorJson")
        val l = KnotVectorJson.toJsonStr(k).parseJson().let { KnotVectorJson.fromJson(it) }
        Assert.assertThat(l, CoreMatchers.`is`(closeTo(k)))
    }

}