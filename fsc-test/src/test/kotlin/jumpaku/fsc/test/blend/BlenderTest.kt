package jumpaku.fsc.test.blend

import io.vavr.API
import io.vavr.Tuple2
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.bspline.shouldEqualToBSpline
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.blend.BlendResult
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.generate.FscGenerator
import org.amshove.kluent.shouldBe
import org.junit.Test

class BlenderTest {

    val urlString = "/jumpaku/fsc/test/blend/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val blender = Blender(
            samplingSpan = 1.0/128,
            blendingRate = 0.5,
            evaluatePath = { it.grade })

    @Test
    fun testBlend() {
        println("Blend")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val a = resourceText("BlendResult$i.json").parseJson().flatMap { BlendResult.fromJson(it) }.get()
            val e = blender.blend(existing, overlapping)
            a.shouldEqualToBlendResult(e)
        }
    }
}