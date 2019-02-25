package jumpaku.curves.fsc.test.freecurve

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonElement
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.freecurve.Shaper
import jumpaku.curves.fsc.freecurve.SmoothResult
import jumpaku.curves.fsc.freecurve.Smoother
import org.amshove.kluent.shouldEqualTo
import org.junit.Test
import org.junit.jupiter.api.Assertions
import java.io.File
import java.time.Duration

class ShaperTest {

    val parent = "src/test/resources/jumpaku/curves/fsc/test/freecurve"

    fun parseSmoothResult(name: String): SmoothResult = File(parent, name + "Result.json").parseJson().tryMap { json ->
        SmoothResult(
                json["conicSections"].array.map { ConicSection.fromJson(it) },
                json["cubicBeziers"].array.map { Bezier.fromJson(it) })
    }.orThrow()

    val shaper = Shaper(
            segmenter = Segmenter(Segmenter.defaultIdentifier),
            smoother = Smoother(
                    pruningFactor = 2.0,
                    nFitSamples = 33,
                    fscSampleSpan = 0.02)) {
        it.domain.sample(0.1)
    }

    fun parseJsonBSpline(name: String): JsonElement = File(parent, name + "Fsc.json").parseJson().orThrow()

    @Test
    fun testShape() {
        println("Shape")
        listOf("swan", "flag", "yacht").forEach { name ->
            val s = BSpline.fromJson(parseJsonBSpline(name))
            val (_, _, actual) = shaper.shape(s)
            val expected = parseSmoothResult(name)
            actual.conicSections.size.shouldEqualTo(expected.conicSections.size)
            actual.cubicBeziers.size.shouldEqualTo(expected.cubicBeziers.size)
        }
    }

    @Test
    fun testShape_Time() {
        println("Shape_Time")
        listOf("swan", "flag", "yacht").forEach { name ->
            val s = BSpline.fromJson(parseJsonBSpline(name))
            shaper.shape(s)//warming up
        }
        listOf("swan", "flag", "yacht").forEach { name ->
            val s = BSpline.fromJson(parseJsonBSpline(name))
            val b = System.nanoTime()
            Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000)) {
                shaper.shape(s)
                println("    $name: ${(System.nanoTime() - b) * 1e-9} [s]")
            }
        }
    }
}