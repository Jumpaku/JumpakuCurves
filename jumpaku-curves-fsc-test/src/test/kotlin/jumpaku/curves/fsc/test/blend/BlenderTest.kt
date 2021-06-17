package jumpaku.curves.fsc.test.blend

import jumpaku.commons.json.parseJson
import jumpaku.commons.option.json.OptionJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.core.test.curve.bspline.closeTo
import jumpaku.curves.fsc.generate.Fuzzifier
//import jumpaku.curves.fsc.blend.BlendDataJson
import jumpaku.curves.fsc.blend.Blender
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class BlenderTest {

    val urlString = "/jumpaku/curves/fsc/test/merge/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val merger: Blender = Blender(
            degree = 3,
            knotSpan = 0.1,
            extendDegree = 2,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            bandWidth = 0.01,
            fuzzifier = Fuzzifier.Linear(0.004, 0.003))

/*
    @Test
    fun testGenerate() {
        println("Generate")
        for (i in 0..4) {
            val bdOpt = resourceText("BlendDataOpt$i.json")
                    .parseJson().let { OptionJson.fromJson(it).map { BlendDataJson.fromJson(it) } }
            val expected = resourceText("BlendedFscOpt$i.json")
                    .parseJson().let { OptionJson.fromJson(it).map { BSplineJson.fromJson(it) } }
            val actual = bdOpt.map { merger.generate(it) }
            assertThat(actual.isDefined, `is`(expected.isDefined))
            if (actual.isDefined) {
                assertThat(actual.orThrow(), `is`(closeTo(expected.orThrow(),1e-7)))
            }
        }
    }

    @Test
    fun testTryMerge() {
        println("TryMerge")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val expected = resourceText("BlendedFscOpt$i.json")
                    .parseJson().let { OptionJson.fromJson(it).map { BSplineJson.fromJson(it) } }
            val actual = merger.tryMerge(existing, overlapping)
            assertThat(actual is , `is`(expected.isDefined))
            if (actual.isDefined) {
                assertThat(actual.orThrow(), `is`(closeTo(expected.orThrow(),1e-7)))
            }
        }
    }
 */
}

