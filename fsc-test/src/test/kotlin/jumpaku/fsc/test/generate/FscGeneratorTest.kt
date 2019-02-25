package jumpaku.fsc.test.generate

import com.github.salomonbrys.kotson.array
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.generate.DataPreparer
import jumpaku.fsc.generate.Generator
import jumpaku.fsc.generate.LinearFuzzifier
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Test
import org.junit.jupiter.api.Assertions
import java.time.Duration

class FscGeneratorTest {

    val urlString = "/jumpaku/fsc/test/generate/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            preparer = DataPreparer(0.1 / 3, 0.1, 0.1, 2),
            fuzzifier = LinearFuzzifier(0.004, 0.003))

    @Test
    fun testGenerate() {
        println("Generate")
        (0..3).forEach { i ->
            val data = resourceText("Data$i.json").parseJson().orThrow().array.map { ParamPoint.fromJson(it) }
            val e = resourceText("Fsc$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val a = generator.generate(data)
            a.evaluateAll(100).zip(e.evaluateAll(100)).forEach { (actual, expected) ->
                actual.isPossible(expected).value.shouldBeGreaterThan(0.85)
            }
        }
    }

    @Test
    fun testGenerate_Time() {
        println("Generate_Time")
        val data = (0..3).map { resourceText("Data$it.json").parseJson().orThrow().array.map { ParamPoint.fromJson(it) } }
        data.forEach { generator.generate(it) }
        data.forEachIndexed { i, d ->
            val b = System.nanoTime()
            Assertions.assertTimeoutPreemptively(Duration.ofMillis(300)) {
                generator.generate(d)
                println("    $i: ${d.last().param - d.first().param}, ${(System.nanoTime() - b) * 1e-9} [s]")
            }
        }
    }

}
