package jumpaku.curves.fsc.test.blend

import jumpaku.commons.control.Option
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.blend.BlendData
import jumpaku.curves.fsc.blend.Blender
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test

class BlenderTest {

    val urlString = "/jumpaku/curves/fsc/test/blend/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.5,
            threshold = Grade(1e-10))

    @Test
    fun testBlend() {
        println("Blend")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val expected = resourceText("BlendDataOpt$i.json")
                    .parseJson().value().flatMap { Option.fromJson(it).map { BlendData.fromJson(it) } }
            val actual = blender.blend(existing, overlapping)
            assertThat(actual.blendedData.isDefined, `is`(expected.isDefined))
            if (actual.blendedData.isDefined) {
                assertThat(actual.grade, `is`(greaterThan(blender.threshold)))
                assertThat(actual.blendedData.orThrow(), `is`(closeTo(expected.orThrow())))
            }
        }
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = blender.toString().parseJson().tryMap { Blender.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(blender)))
    }
}
