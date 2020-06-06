package jumpaku.curves.core.test.curve

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.IntervalJson
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class IntervalJsonTest {

    val i = Interval(-2.3, 3.4)

    @Test
    fun testIntervalJson() {
        println("IntervalJson")
        val a = IntervalJson.toJson(i).toString().parseJson().let { IntervalJson.fromJson(it) }
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(i)))
    }
}