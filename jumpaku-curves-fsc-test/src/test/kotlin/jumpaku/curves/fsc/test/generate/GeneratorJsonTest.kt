package jumpaku.curves.fsc.test.generate

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.GeneratorJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class GeneratorJsonTest {

    val generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.1 / 3,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(0.004, 0.003))

    @Test
    fun testGeneratorJson() {
        println("GeneratorJson")
        val a = GeneratorJson.toJsonStr(generator).parseJson().let { GeneratorJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(closeTo(generator)))
    }
}