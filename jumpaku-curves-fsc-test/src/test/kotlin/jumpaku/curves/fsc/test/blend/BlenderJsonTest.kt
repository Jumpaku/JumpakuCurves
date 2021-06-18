package jumpaku.curves.fsc.test.blend

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.blend.BlenderJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class BlenderJsonTest {

    val blender: Blender = Blender(
            degree = 3,
            knotSpan = 0.1,
            extendDegree = 2,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            bandWidth = 0.01,
            fuzzifier = Fuzzifier.Linear(0.004, 0.003))

    @Test
    fun testBlenderJson() {
        println("BlenderJson")
        val a = BlenderJson.fromJson(BlenderJson.toJsonStr(blender).parseJson())
        Assert.assertThat(a, Matchers.`is`(closeTo(blender)))
    }


}