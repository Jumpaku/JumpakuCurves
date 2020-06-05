package jumpaku.curves.fsc.test.merge

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.merge.Merger
import jumpaku.curves.fsc.merge.MergerJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class MergerJsonTest {

    val merger: Merger = Merger(
            degree = 3,
            knotSpan = 0.1,
            extendDegree = 2,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            bandWidth = 0.01,
            fuzzifier = Fuzzifier.Linear(0.004, 0.003))

    @Test
    fun testMergerJson() {
        println("MergerJson")
        val a = MergerJson.fromJson(MergerJson.toJsonStr(merger).parseJson())
        Assert.assertThat(a, Matchers.`is`(closeTo(merger)))
    }


}