package jumpaku.curves.fsc.test.blend

import com.github.salomonbrys.kotson.array
import jumpaku.commons.control.Option
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.weighted
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.fsc.blend.Blender
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class BlenderTest {

    val urlString = "/jumpaku/curves/fsc/test/blend/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val blender: Blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.65,
            possibilityThreshold = Grade(1e-10))

    @Test
    fun testBlend() {
        println("Blend")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val expected = resourceText("BlendResult$i.json").parseJson().tryMap {
                Option.fromJson(it).map { it.array.map { WeightedParamPoint.fromJson(it) } }
            }.orThrow()
            val actual = blender.blend(existing, overlapping)
            assertThat(actual.isDefined, `is`(expected.isDefined))
            if (actual.isDefined) {
                assertThat(actual.orThrow().size, `is`(expected.orThrow().size))
                actual.orThrow().zip(expected.orThrow()).forEach { (a, e) ->
                    assertThat(a, `is`(closeTo(e, 1e-6)))
                }
            }
        }
    }
}