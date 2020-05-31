package jumpaku.curves.fsc.benchmark.generate

import com.github.salomonbrys.kotson.array
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.ParamPointJson
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration


open class GeneratorBenchmark {

    fun resourceText(name: String): String {
        val urlString = "/jumpaku/curves/fsc/benchmark/generate/"
        return javaClass.getResource(urlString + name).readText()
    }

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.1 / 3,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(0.004, 0.003))

    val data: List<List<ParamPoint>> = (0..3).map {
        resourceText("Data$it.json").parseJson().array.map { ParamPointJson.fromJson(it) }
    }

    @Test
    fun benchmarkGenerator() {

        data.forEach { ps ->
            repeat(20) { generator.generate(ps) }
        }
        assertTimeout(Duration.ofMillis(150)) {
            generator.generate(data[0])
        }
        assertTimeout(Duration.ofMillis(150)) {
            generator.generate(data[1])
        }
        assertTimeout(Duration.ofMillis(150)) {
            generator.generate(data[2])
        }
        assertTimeout(Duration.ofMillis(150)) {
            generator.generate(data[3])
        }
    }
}
