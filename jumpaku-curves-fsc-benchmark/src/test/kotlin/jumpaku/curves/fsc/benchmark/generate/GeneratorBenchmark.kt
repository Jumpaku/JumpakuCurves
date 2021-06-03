package jumpaku.curves.fsc.benchmark.generate

import com.github.salomonbrys.kotson.array
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.ParamPointJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineDerivative
import jumpaku.curves.core.curve.bspline.Nurbs
import jumpaku.curves.core.curve.bspline2.KnotVector
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.weighted
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Fuzzifier2
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.Generator2
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.lang.Math.abs
import java.time.Duration
import kotlin.random.Random
import kotlin.system.measureNanoTime


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
        fuzzifier = Fuzzifier.Linear(0.004, 0.003)
    )

    val data: List<List<ParamPoint>> = (0..3).map {
        resourceText("Data$it.json").parseJson().array.map { ParamPointJson.fromJson(it) }
    }

    @Test
    fun benchmarkGenerator() {
        val time = 100L
        println("Generator <= $time ms")

        data.forEach { ps ->
            repeat(20) { generator.generate(ps) }
        }
        assertTimeout(Duration.ofMillis(time)) {
            generator.generate(data[0])
        }
        assertTimeout(Duration.ofMillis(time)) {
            generator.generate(data[1])
        }
        assertTimeout(Duration.ofMillis(time)) {
            generator.generate(data[2])
        }
        assertTimeout(Duration.ofMillis(time)) {
            generator.generate(data[3])
        }
    }

    @Test
    fun benchmarkEvaluateAll() {
        val time = 100L
        println("EvaluateAll <= $time ms")

        data.forEach { ps -> repeat(20) { generator.generate(ps) } }
        val times = 500
        val s = generator.generate(data[3])
        val s2 = jumpaku.curves.core.curve.bspline2.BSpline(
            s.controlPoints,
            KnotVector.of(s.degree, s.knotVector.extractedKnots)
        )

        repeat(times) { s2.evaluateAll(0.1).size }
        repeat(times) { s.evaluateAll(0.1).size }
        var n: Int? = null
        var m: Int? = null
        val t0 = measureNanoTime { repeat(times) { n = s.evaluateAll(0.1).size } }
        val t1 = measureNanoTime { repeat(times) { m = s2.evaluateAll(0.1).size } }
        val t2 = measureNanoTime { repeat(times) { m = s2.evaluateAll(0.1).size } }
        val t3 = measureNanoTime { repeat(times) { n = s.evaluateAll(0.1).size } }

        val t4 = measureNanoTime { repeat(times) { n = s.evaluateAll(0.1).size } }
        val t5 = measureNanoTime { repeat(times) { m = s2.evaluateAll(0.1).size } }
        val t6 = measureNanoTime { repeat(times) { m = s2.evaluateAll(0.1).size } }
        val t7 = measureNanoTime { repeat(times) { n = s.evaluateAll(0.1).size } }
        println("${t0 * 1e-9} $n")
        println("${t1 * 1e-9} $m")
        println("${t2 * 1e-9} $m")
        println("${t3 * 1e-9} $m")
        println("${t4 * 1e-9} $n")
        println("${t5 * 1e-9} $m")
        println("${t6 * 1e-9} $m")
        println("${t7 * 1e-9} $m")


    }

    @Test
    fun benchmarkGenerator2() {
        val time = 100L
        println("Generator2 <= $time ms")

        val times = 1
        val g1 = generator
        val g2 = Generator2(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.1 / 3,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier2.Linear(0.004, 0.003)
        )
/*
        var s1: BSpline? = null
        var s2: jumpaku.curves.core.curve.bspline2.BSpline? = null
        val t1 = measureNanoTime { repeat(times) { s1 = g1.generate(data[3]) } }
        val t2 = measureNanoTime { repeat(times) { s2 = g2.generate(data[3]) } }
        val t3 = measureNanoTime { repeat(times) { s2 = g2.generate(data[3]) } }
        val t4 = measureNanoTime { repeat(times) { s1 = g1.generate(data[3]) } }
        val t5 = measureNanoTime { repeat(times) { s1 = g1.generate(data[3]) } }
        val t6 = measureNanoTime { repeat(times) { s2 = g2.generate(data[3]) } }
        val t7 = measureNanoTime { repeat(times) { s2 = g2.generate(data[3]) } }
        val t8 = measureNanoTime { repeat(times) { s1 = g1.generate(data[3]) } }
        println("${t1 * 1e-9} ${s1!!.domain.span}")
        println("${t2 * 1e-9} ${s1!!.domain.span}")
        println("${t3 * 1e-9} ${s1!!.domain.span}")
        println("${t4 * 1e-9} ${s2!!.domain.span}")
        println("${t5 * 1e-9} ${s1!!.domain.span}")
        println("${t6 * 1e-9} ${s1!!.domain.span}")
        println("${t7 * 1e-9} ${s1!!.domain.span}")
        println("${t8 * 1e-9} ${s2!!.domain.span}")
*/
        val s3 = g1.generate(data[3])
        val s4 = g2.generate(data[3])
        println("${s3.knotVector.extractedKnots.take(10)}")
        println("${s4.knotVector.take(10)}")
        println("${s3.knotVector.extractedKnots.takeLast(10)}")
        println("${s4.knotVector.takeLast(10)}")
        println("${s3.knotVector.extractedKnots.size} ${s4.knotVector.size}")
        println("${s3.controlPoints.size} ${s4.controlPoints.size}")
        for (i in s3.controlPoints.indices) {
            val (x3, y3, z3, r3) = s3.controlPoints[i]
            val (x4, y4, z4, r4) = s4.controlPoints[i]
            if (kotlin.math.abs(x3 - x4) > 1e-7) println("$i x: $x3, $x4")
            if (kotlin.math.abs(y3 - y4) > 1e-7) println("$i y: $y3, $y4")
            if (kotlin.math.abs(z3 - z4) > 1e-7) println("$i z: $z3, $z4")
            if (kotlin.math.abs(r3 - r4) > 1e-7) println("$i r: $r3, $r4")
        }
        println("")
    }

    @Test
    fun benchmarkDifferentiate() {
        val time = 100L
        println("Generator2 <= $time ms")

        data.forEach { ps ->
            repeat(100) { generator.generate(ps) }
        }
        val times = 1000
        val g1 = generator
        val g2 = Generator2(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.1 / 3,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier2.Linear(0.004, 0.003)
        )

        //repeat(times) { g1.generate(data[3]) }
        //repeat(times) { g2.generate(data[3]) }

        val s1:BSpline = g1.generate(data[3])
        val s2:jumpaku.curves.core.curve.bspline2.BSpline = g2.generate(data[3])
        var n1 = 0
        var n2 = 0
        val t1 = measureNanoTime { repeat(times) { n1 += s1.evaluateAll(2000).size } }
        val t2 = measureNanoTime { repeat(times) { n2 += s2.evaluateAll(2000).size } }
        val t3 = measureNanoTime { repeat(times) { n2 += s2.evaluateAll(2000).size } }
        val t4 = measureNanoTime { repeat(times) { n1 += s1.evaluateAll(2000).size } }
        val t5 = measureNanoTime { repeat(times) { n1 += s1.evaluateAll(2000).size } }
        val t6 = measureNanoTime { repeat(times) { n2 += s2.evaluateAll(2000).size } }
        val t7 = measureNanoTime { repeat(times) { n2 += s2.evaluateAll(2000).size } }
        val t8 = measureNanoTime { repeat(times) { n1 += s1.evaluateAll(2000).size } }
        println("${t1 * 1e-9} ${n1}")
        println("${t2 * 1e-9} ${n1}")
        println("${t3 * 1e-9} ${n1}")
        println("${t4 * 1e-9} ${n2}")
        println("${t5 * 1e-9} ${n1}")
        println("${t6 * 1e-9} ${n1}")
        println("${t7 * 1e-9} ${n1}")
        println("${t8 * 1e-9} ${n2}")
    }
}
