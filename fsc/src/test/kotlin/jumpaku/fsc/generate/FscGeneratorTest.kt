package jumpaku.fsc.generate

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import jumpaku.core.affine.Vector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.curve.fuzzyCurveAssertThat
import jumpaku.core.curve.paramPoint
import jumpaku.core.json.parseToJson
import jumpaku.core.json.prettyGson
import org.assertj.core.api.Assertions
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class FscGeneratorTest {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/generate/")


    @Test
    fun testGenerate() {
        println("Generate")
        for (i in 0..5) {
            val dataFile = path.resolve("FscGenerationData$i.json").toFile()
            val data = Array.ofAll(dataFile.readText().parseToJson().get().array.map { it.paramPoint })
            val a = FscGenerator(3, 0.1, generateFuzziness = { crisp, ts ->
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
            val e = path.resolve("FscGenerationFsc$i.json").toFile().readText().parseToJson().get().bSpline

            fuzzyCurveAssertThat(a).isEqualToFuzzyCurve(e, 12.0, 30)
        }
    }
}
