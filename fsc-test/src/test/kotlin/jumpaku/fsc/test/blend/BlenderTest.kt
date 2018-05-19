package jumpaku.fsc.test.blend

import io.vavr.API
import io.vavr.Tuple2
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.bspline.shouldBeBSpline
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

    val generator = FscGenerator(3, 0.1, generateFuzziness = { crisp, ts ->
        val derivative1 = crisp.derivative
        val derivative2 = derivative1.derivative
        val velocityCoefficient = 0.004
        val accelerationCoefficient = 0.003
        ts.map {
            val v = derivative1(it).length()
            val a = derivative2(it).length()
            velocityCoefficient * v + a * accelerationCoefficient + 1.0
        }
    })

    val blender = Blender(
            1.0/128,
            0.5,
            generator,
            { _ -> grade.value })

    @Test
    fun testBlend() {
        println("Blend")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val a = resourceText("BlendResult$i.json").parseJson().flatMap { BlendResult.fromJson(it) }.get()
            val e = blender.blend(existing, overlapping)
            a.blended.isDefined.shouldBe(e.blended.isDefined)
            API.For(a.blended, e.blended).`yield` { t, u -> Tuple2(t, u) }.forEach { (t, u) ->
                t.shouldBeBSpline(u)
            }
        }
    }
}