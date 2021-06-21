package jumpaku.curves.fsc.test.blend

import jumpaku.commons.json.parseJson
import jumpaku.commons.option.json.OptionJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.test.curve.bspline.closeTo
import jumpaku.curves.fsc.blend.BlendResult
import jumpaku.curves.fsc.generate.Fuzzifier
//import jumpaku.curves.fsc.blend.BlendDataJson
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test

class BlenderTest {

    val urlString = "/jumpaku/curves/fsc/test/merge/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val blender: Blender = Blender(
        degree = 3,
        knotSpan = 0.1,
        extendDegree = 2,
        extendInnerSpan = 0.1,
        extendOuterSpan = 0.1,
        bandWidth = 0.01,
        samplingSpan = 0.01,
        overlapThreshold = Grade(0.5),
        blendRate = 0.75,
        fuzzifier = Fuzzifier.Linear(0.004, 0.003)
    )

    @Test
    fun testTryBlend() {
        println("TryBlend")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val expected = resourceText("BlendedFscOpt$i.json")
                .parseJson().let { OptionJson.fromJson(it).map { BSplineJson.fromJson(it) } }
            val actual = blender.tryBlend(existing, overlapping)
            assertThat(actual is BlendResult.Blended, `is`(expected.isDefined))
            if (actual is BlendResult.Blended) {
                val a = reparametrize(actual.blended)
                val e = reparametrize(expected.orThrow())
                assertThat(a.isPossible(e, 50), `is`(greaterThan(Grade(0.7))))
            }
        }
    }
}

