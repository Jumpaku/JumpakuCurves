package jumpaku.curves.fsc.test.freecurve

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonElement
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.freecurve.Shaper
import jumpaku.curves.fsc.freecurve.SmoothResult
import jumpaku.curves.fsc.freecurve.Smoother
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import java.time.Duration

class ShaperTest {

    val urlString = "/jumpaku/curves/fsc/test/freecurve/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    fun parseSmoothResult(name: String): SmoothResult = resourceText(name + "Result.json").parseJson().tryMap { json ->
        SmoothResult(
                json["conicSections"].array.map { ConicSection.fromJson(it) },
                json["cubicBeziers"].array.map { Bezier.fromJson(it) })
    }.orThrow()

    val shaper = Shaper(
            segmenter = Segmenter(Segmenter.defaultIdentifier),
            smoother = Smoother(
                    pruningFactor = 2.0,
                    nFitSamples = 33,
                    fscSampleSpan = 0.02),
            sampleFsc = { it.domain.sample(50) })

    fun parseJsonBSpline(name: String): JsonElement = resourceText(name + "Fsc.json").parseJson().orThrow()

    @Test
    fun testShape() {
        println("Shape")
        listOf("swan", "flag", "yacht").forEach { name ->
            val s = BSpline.fromJson(parseJsonBSpline(name))
            val (_, _, actual) = shaper.shape(s)
            val expected = parseSmoothResult(name)
            assertThat("$name: ", actual.conicSections.size, `is`(expected.conicSections.size))
            assertThat(actual.cubicBeziers.size, `is`(expected.cubicBeziers.size))
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
            assertTimeoutPreemptively(Duration.ofMillis(1200)) {
                shaper.shape(s)
                println("    $name: ${(System.nanoTime() - b) * 1e-9} [s]")
            }
        }
    }
}