package jumpaku.curves.fsc.test.generate

import com.github.salomonbrys.kotson.array
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPointJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test

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
            val data = resourceText("Data$i.json").parseJson().array.map { ParamPointJson.fromJson(it) }
            val e = resourceText("Fsc$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val a = generator.generate(data)
            a.evaluateAll(100).zip(e.evaluateAll(100)).forEach { (actual, expected) ->
                assertThat(actual.isPossible(expected).value, `is`(greaterThan(0.85)))
            }
        }
    }
}

