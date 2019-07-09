package jumpaku.curves.fsc.test.generate

import com.github.salomonbrys.kotson.array
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import java.time.Duration

class FscGeneratorTest {

    val urlString = "/jumpaku/curves/fsc/test/generate/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.1 / 3,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(0.004, 0.003))

    @Test
    fun testGenerate() {
        println("Generate")
        (0..3).forEach { i ->
            val data = resourceText("Data$i.json").parseJson().orThrow().array.map { ParamPoint.fromJson(it) }
            val e = resourceText("Fsc$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val a = generator.generate(data)
            a.evaluateAll(100).zip(e.evaluateAll(100)).forEach { (actual, expected) ->
                assertThat(actual.isPossible(expected).value, `is`(greaterThan(0.85)))
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
            assertTimeoutPreemptively(Duration.ofMillis(300)) {
                generator.generate(d)
                println("    $i: ${d.last().param - d.first().param}, ${(System.nanoTime() - b) * 1e-9} [s]")
            }
        }
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = generator.toString().parseJson().tryMap { Generator.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(generator)))
    }
}
