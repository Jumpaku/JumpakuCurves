package jumpaku.curves.fsc.test.generate

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.FuzzifierJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class FuzzifierJsonTest {

    val l = Fuzzifier.Linear(velocityCoefficient = 3.0, accelerationCoefficient = 0.1)

    @Test
    fun testFuzzifierJson() {
        println("FuzzifierJson")
        val a = FuzzifierJson.toJsonStr(l).parseJson().let { FuzzifierJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(closeTo(l)))
    }
}