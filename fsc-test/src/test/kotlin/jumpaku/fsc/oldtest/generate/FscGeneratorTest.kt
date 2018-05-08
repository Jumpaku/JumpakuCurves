package jumpaku.fsc.oldtest.generate

import com.github.salomonbrys.kotson.array
import io.vavr.collection.Array
import jumpaku.core.affine.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.testold.curve.bspline.bSplineAssertThat
import jumpaku.fsc.generate.FscGenerator
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class FscGeneratorTest {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/test/generate/")


    @Test
    fun testGenerate() {
        println("Generate")
        (0..2).forEach { i ->
            val data = Array.ofAll(path.resolve("Data$i.json").parseJson().get()
                    .array.flatMap { ParamPoint.fromJson(it) })
            val e = path.resolve("Fsc$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val a = FscGenerator(degree = 3, knotSpan = 0.1, generateFuzziness = { crisp, ts ->
                val derivative1 = crisp.derivative
                val derivative2 = derivative1.derivative
                val velocityCoefficient = 0.004
                val accelerationCoefficient = 0.003
                ts.map {
                    val v = derivative1(it).length()
                    val a = derivative2(it).length()
                    velocityCoefficient * v + a * accelerationCoefficient + 1.0
                }
            }).generate(data)
           bSplineAssertThat(a).`as`("$i").isEqualToBSpline(e)
        }
    }
}
