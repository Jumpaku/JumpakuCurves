package jumpaku.curves.fsc.benchmark.blend

import com.github.salomonbrys.kotson.array
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.ParamPointJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.blend.BlendResult
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration
import kotlin.system.measureNanoTime


open class BlenderBenchmark {

    fun resourceText(name: String): String {
        val urlString = "/jumpaku/curves/fsc/benchmark/blend/"
        return javaClass.getResource(urlString + name).readText()
    }

    val generator: Generator = Generator(
        degree = 3,
        knotSpan = 0.1,
        fillSpan = 0.1 / 3,
        extendInnerSpan = 0.1,
        extendOuterSpan = 0.1,
        extendDegree = 2,
        fuzzifier = Fuzzifier.Linear(0.004, 0.003)
    )

    val blender = Blender.derive(
        generator = generator,
        samplingSpan = 0.01,
        overlapThreshold = Grade.FALSE,
        blendRate = 0.5,
        bandWidth = 0.01
    )

    val existing: List<BSpline> = (0..4).map {
        resourceText("BlendExisting$it.json").parseJson().let { BSplineJson.fromJson(it) }
    }
    val overlapping: List<BSpline> = (0..4).map {
        resourceText("BlendOverlapping$it.json").parseJson().let { BSplineJson.fromJson(it) }
    }

    @Test
    fun benchmarkBlender() {
        val time = 25L
        println("Blender <= $time ms")

        repeat(100) {
            blender.tryBlend(existing[0], overlapping[0])
        }

        assertTimeout(Duration.ofMillis(time)) {
            blender.tryBlend(existing[0], overlapping[0])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender.tryBlend(existing[1], overlapping[1])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender.tryBlend(existing[2], overlapping[2])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender.tryBlend(existing[3], overlapping[3])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender.tryBlend(existing[4], overlapping[4])
        }
    }
}
