package jumpaku.curves.fsc.test.freecurve

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.freecurve.Shaper
import jumpaku.curves.fsc.freecurve.ShaperJson
import jumpaku.curves.fsc.freecurve.Smoother
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class ShaperJsonTest {

    val shaper = Shaper(
            segmenter = Segmenter(
                    identifier = Open4Identifier(
                            nSamples = 25,
                            nFmps = 15)),
            smoother = Smoother(
                    pruningFactor = 2.0,
                    samplingFactor = 33),
            sampler = Shaper.Sampler.ByFixedNumber(50))

    @Test
    fun testShaperJson() {
        println("ShaperJson")
        val a = ShaperJson.toJsonStr(shaper).parseJson().let { ShaperJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(closeTo(shaper)))
    }
}