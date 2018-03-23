package jumpaku.fsc.blend

import io.vavr.API
import io.vavr.Tuple2
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.curve.bspline.bSplineAssertThat
import jumpaku.core.json.parseJson
import jumpaku.fsc.generate.FscGenerator
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.nio.file.Paths

class BlenderTest {

    private val path = Paths.get("./src/test/resources/jumpaku/fsc/blend/")

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
            { _ -> grade.value }
    )

    @Test
    fun testBlend() {
        println("Blend")
        for (i in 0..4) {
            val existing = path.resolve("BlendExisting$i.json").toFile().readText().parseJson().get().bSpline
            val overlapping = path.resolve("BlendOverlapping$i.json").toFile().readText().parseJson().get().bSpline
            val a = path.resolve("BlendResult$i.json").parseJson().flatMap { BlendResult.fromJson(it) }.get()
            val e = blender.blend(existing, overlapping)
            //println("${a.blended.isDefined}, ${e.blended.isDefined}")
            assertThat(a.blended.isDefined).isEqualTo(e.blended.isDefined)
            API.For(a.blended, e.blended).`yield` { t, u -> Tuple2(t, u) }.forEach { (t, u) ->
                bSplineAssertThat(t).isEqualToBSpline(u)
            }
        }
    }
}