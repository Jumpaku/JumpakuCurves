package jumpaku.curves.fsc.benchmark.identify

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration

class IdentifierBenchmark {

    fun resourceText(name: String): String {
        val urlString = "/jumpaku/curves/fsc/benchmark/identify/primitive/reference/"
        return javaClass.getResource(urlString + name).readText()
    }

    val identifier = Open4Identifier(nSamples = 25, nFmps = 15)

    val data = listOf(
            "FscCA0.json",
            "FscCA1.json",
            "FscCA2.json",
            "FscEA0.json",
            "FscEA1.json",
            "FscEA2.json",
            "FscEA3.json",
            "FscEA4.json",
            "FscFO0.json",
            "FscL0.json",
            "FscL1.json").associateWith { resourceText(it).parseJson().let { BSplineJson.fromJson(it) } }

    @Test
    fun benchmarkIdentifier() {
        data.forEach {
            val s = reparametrize(it.value)
            repeat(1000){ identifier.identify(s) }
        }

        val s = data.mapValues { reparametrize(it.value) }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscCA0.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscCA1.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscCA2.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscEA0.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscEA1.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscEA2.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscEA3.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscEA4.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscFO0.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscL0.json"]!!)
        }
        assertTimeout(Duration.ofMillis(3)) {
            identifier.identify(s["FscL1.json"]!!)
        }
    }
}