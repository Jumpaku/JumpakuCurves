package jumpaku.fsc.blend

import io.vavr.API
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.curve.bspline.bSplineAssertThat
import jumpaku.core.json.parseToJson
import jumpaku.fsc.generate.FscGenerator
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.nio.file.Paths

class BlenderTest {

    private val path = Paths.get("./src/test/resources/jumpaku/fsc/blend/")

    @Test
    fun testBlend() {
        println("Blend")
        for (i in 0..4) {
            val existing = path.resolve("BlendExisting$i.json").toFile().readText().parseToJson().get().bSpline
            val overlapping = path.resolve("BlendOverlapping$i.json").toFile().readText().parseToJson().get().bSpline
            val (_, _, eOpt) = path.resolve("BlendResult$i.json").toFile().readText().parseToJson().get().blendResult
            val (_, _, aOpt) = Blender(1.0/128, 0.5,
                    FscGenerator(3, 0.1, generateFuzziness = { crisp, ts ->
                        val derivative1 = crisp.derivative
                        val derivative2 = derivative1.derivative
                        val velocityCoefficient = 0.004
                        val accelerationCoefficient = 0.003
                        ts.map {
                            val v = derivative1(it).length()
                            val a = derivative2(it).length()
                            velocityCoefficient * v + a * accelerationCoefficient + 1.0
                        }
                    }),
                    { _ -> grade.value }
            ).blend(existing, overlapping)
            assertThat(aOpt.isDefined).isEqualTo(eOpt.isDefined)
            API.For(aOpt, eOpt).`yield` { a, e -> bSplineAssertThat(a).isEqualToBSpline(e) }
        }
    }
}