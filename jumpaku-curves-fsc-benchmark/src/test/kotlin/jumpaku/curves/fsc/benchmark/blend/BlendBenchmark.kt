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
import jumpaku.curves.fsc.merge.Merger
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

    val blender1 = Merger.derive(
        generator = generator,
        samplingSpan = 0.01,
        overlapThreshold = Grade(0.5),
        mergeRate = 0.5,
        bandWidth = 0.01
    )

    val blender2 = Blender.derive(
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
    fun benchmarkMerger() {
        val time = 100L
        println("Blender <= $time ms")

        repeat(20) {
            blender1.tryMerge(existing[0], overlapping[0])
        }

        assertTimeout(Duration.ofMillis(time)) {
            blender1.tryMerge(existing[0], overlapping[0])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender1.tryMerge(existing[1], overlapping[1])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender1.tryMerge(existing[2], overlapping[2])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender1.tryMerge(existing[3], overlapping[3])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender1.tryMerge(existing[4], overlapping[4])
        }
    }

    @Test
    fun benchmarkBlender() {
        val time = 25L
        println("Blender <= $time ms")

        repeat(100) {
            blender2.tryBlend(existing[0], overlapping[0])
        }

        assertTimeout(Duration.ofMillis(time)) {
            blender2.tryBlend(existing[0], overlapping[0])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender2.tryBlend(existing[1], overlapping[1])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender2.tryBlend(existing[2], overlapping[2])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender2.tryBlend(existing[3], overlapping[3])
        }
        assertTimeout(Duration.ofMillis(time)) {
            blender2.tryBlend(existing[4], overlapping[4])
        }
    }

    @Test
    fun benchmark() {
        println(System.nanoTime())
        fun merge(time: Int): Boolean {
            var r = true
            repeat(time) { r = r && blender1.tryMerge(existing[0], overlapping[0]).isDefined }
            repeat(time) { r = r && blender1.tryMerge(existing[1], overlapping[1]).isDefined }
            repeat(time) { r = r && blender1.tryMerge(existing[2], overlapping[2]).isDefined }
            repeat(time) { r = r && blender1.tryMerge(existing[3], overlapping[3]).isDefined }
            repeat(time) { r = r && blender1.tryMerge(existing[4], overlapping[4]).isDefined }
            return r
        }

        fun blend(time: Int): Boolean {
            var r = true
            repeat(time) { r = r && blender2.tryBlend(existing[0], overlapping[0]) is BlendResult.Blended }
            repeat(time) { r = r && blender2.tryBlend(existing[1], overlapping[1]) is BlendResult.Blended }
            repeat(time) { r = r && blender2.tryBlend(existing[2], overlapping[2]) is BlendResult.Blended }
            repeat(time) { r = r && blender2.tryBlend(existing[3], overlapping[3]) is BlendResult.Blended }
            repeat(time) { r = r && blender2.tryBlend(existing[4], overlapping[4]) is BlendResult.Blended }
            return r
        }

        merge(100)
        blend(100)
        val times = 100
        println("${measureNanoTime { merge(times) } * 1e-9/times}")
        println("${measureNanoTime { blend(times) } * 1e-9/times}")
        println("${measureNanoTime { merge(times) } * 1e-9/times}")
        println("${measureNanoTime { blend(times) } * 1e-9/times}")
        println("${measureNanoTime { merge(times) } * 1e-9/times}")
        println("${measureNanoTime { blend(times) } * 1e-9/times}")
        println("${measureNanoTime { merge(times) } * 1e-9/times}")
        println("${measureNanoTime { blend(times) } * 1e-9/times}")
    }
}
