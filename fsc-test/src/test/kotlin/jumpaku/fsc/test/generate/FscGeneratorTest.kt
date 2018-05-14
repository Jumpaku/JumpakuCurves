package jumpaku.fsc.test.generate

import com.github.salomonbrys.kotson.array
import io.vavr.collection.Array
import jumpaku.core.affine.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.bspline.shouldEqualToBSpline
import jumpaku.fsc.generate.FscGenerator
import org.junit.Test

class FscGeneratorTest {

    val urlString = "/jumpaku/fsc/test/generate/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = FscGenerator(degree = 3, knotSpan = 0.1, generateFuzziness = { crisp, ts ->
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
    @Test
    fun testGenerate() {
        println("Generate")
        (0..2).forEach { i ->
            val data = Array.ofAll(resourceText("Data$i.json").parseJson().get().array.flatMap { ParamPoint.fromJson(it) })
            val e = resourceText("Fsc$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val a = generator.generate(data)
            a.shouldEqualToBSpline(e)
        }
    }
}
