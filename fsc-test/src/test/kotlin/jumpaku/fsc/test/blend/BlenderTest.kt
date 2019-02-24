package jumpaku.fsc.test.blend

import com.github.salomonbrys.kotson.array
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.isCloseTo
import jumpaku.core.util.Option
import jumpaku.fsc.blend.Blender
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class BlenderTest {

    val urlString = "/jumpaku/fsc/test/blend/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val blender = Blender(
            samplingSpan = 1.0/128,
            blendingRate = 0.5,
            minPossibility = Grade(1e-10),
            evaluatePath = { path, _ -> path.grade.value })

    @Test
    fun testBlend() {
        println("Blend")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val expected = resourceText("BlendResult$i.json").parseJson().tryMap {
                Option.fromJson(it).map { it.array.map { ParamPoint.fromJson(it) } }
            }.orThrow()
            val actual = blender.blend(existing, overlapping)
            actual.isDefined.shouldBe(expected.isDefined)
            if (actual.isDefined) {
                actual.orThrow().size.shouldEqualTo(expected.orThrow().size)
                actual.orThrow().zip(expected.orThrow()).forEach { (e, a) ->
                    isCloseTo(a, e)
                }
            }
        }
    }
}