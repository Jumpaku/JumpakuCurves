package jumpaku.curves.fsc.test.blend

import jumpaku.commons.control.Option
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.test.curve.bspline.closeTo
import jumpaku.curves.fsc.blend.BlendData
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class BlendGeneratorTest {

    val urlString = "/jumpaku/curves/fsc/test/blend/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val blendGenerator: BlendGenerator = BlendGenerator(Generator(
            degree = 3,
            knotSpan = 0.1,
            dataPreparer = DataPreparer(0.1 / 3, 0.1, 0.1, 2),
            fuzzifier = Fuzzifier.Linear(0.004, 0.003)), 0.01)


    @Test
    fun testGenerate() {
        println("BlendGenerator.Generate")
        for (i in 0..4) {
            val bdOpt = resourceText("BlendDataOpt$i.json")
                    .parseJson().value().flatMap { Option.fromJson(it).map { BlendData.fromJson(it) } }
            val expected = resourceText("BlendedFscOpt$i.json")
                    .parseJson().value().flatMap { Option.fromJson(it).map { BSpline.fromJson(it) } }
            val actual = bdOpt.map { blendGenerator.generate(it) }
            assertThat(actual.isDefined, `is`(expected.isDefined))
            if (actual.isDefined) {
                assertThat(actual.orThrow(), `is`(closeTo(expected.orThrow())))
            }
        }
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = BlendGenerator.fromJson(blendGenerator.toJsonString().parseJson().orThrow())
        assertThat(a, `is`(closeTo(blendGenerator)))
    }


}