package jumpaku.curves.fsc.benchmark.shape

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.freecurve.Shaper
import jumpaku.curves.fsc.freecurve.Smoother
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration

class ShaperBenchmark {

    fun resourceText(name: String): String {
        val urlString = "/jumpaku/curves/fsc/benchmark/freecurve/"
        return javaClass.getResource(urlString + name).readText()
    }

    val shaper = Shaper(
            segmenter = Segmenter(
                    identifier = Open4Identifier(
                            nSamples = 25,
                            nFmps = 15)),
            smoother = Smoother(
                    pruningFactor = 2.0,
                    samplingFactor = 33),
            sampler = Shaper.Sampler.ByFixedNumber(50))

    val data = listOf("swanFsc.json", "flagFsc.json", "yachtFsc.json").map {
        BSplineJson.fromJson(resourceText(it).parseJson())
    }

    @Test
    fun benchmarkShaper() {

        val time = 300L
        println("Shaper <= $time ms")
        data.forEach { s ->
            repeat(30) { shaper.shape(s) }
        }
        assertTimeout(Duration.ofMillis(time)) {
            shaper.shape(data[0])
        }
        assertTimeout(Duration.ofMillis(time)) {
            shaper.shape(data[1])
        }
        assertTimeout(Duration.ofMillis(time)) {
            shaper.shape(data[2])
        }
    }
}