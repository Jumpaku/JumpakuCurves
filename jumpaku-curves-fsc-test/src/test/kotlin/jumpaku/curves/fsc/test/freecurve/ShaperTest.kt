package jumpaku.curves.fsc.test.freecurve

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonElement
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bezier.BezierJson
import jumpaku.curves.core.curve.bezier.ConicSectionJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.freecurve.Shaper
import jumpaku.curves.fsc.freecurve.SmoothResult
import jumpaku.curves.fsc.freecurve.Smoother
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ShaperTest {

    val urlString = "/jumpaku/curves/fsc/test/freecurve/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    fun parseSmoothResult(name: String): SmoothResult = resourceText(name + "Result.json").parseJson().let { json ->
        SmoothResult(
                json["conicSections"].array.map { ConicSectionJson.fromJson(it) },
                json["cubicBeziers"].array.map { BezierJson.fromJson(it) })
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

    fun parseJsonBSpline(name: String): JsonElement = resourceText(name + "Fsc.json").parseJson()

    @Test
    fun testShape() {
        println("Shape")
        listOf("swan", "flag", "yacht").forEach { name ->
            val s = BSplineJson.fromJson(parseJsonBSpline(name))
            val (_, _, actual) = shaper.shape(s)
            val expected = parseSmoothResult(name)
            assertThat("$name: ", actual.conicSections.size, `is`(expected.conicSections.size))
            assertThat(actual.cubicBeziers.size, `is`(expected.cubicBeziers.size))
        }
    }
/*
    @Test
    fun testShape_Time() {
        println("Shape_Time")
        listOf("swan", "flag", "yacht").forEach { name ->
            val s = BSplineJson.fromJson(parseJsonBSpline(name))
            shaper.shape(s)//warming up
        }
        listOf("swan", "flag", "yacht").forEach { name ->
            val s = BSplineJson.fromJson(parseJsonBSpline(name))
            val b = System.nanoTime()
            assertTimeoutPreemptively(Duration.ofMillis(1200)) {
                shaper.shape(s)
                println("    $name: ${(System.nanoTime() - b) * 1e-9} [s]")
            }
        }
    }
*/
    /*
    @Test
    fun testToString_Segmenter() {
        println("ToString_Segmenter")
        val a = shaper.segmenter.toString().parseJson().let { Segmenter.fromJson(it) }.orThrow()
        assertThat(a, `is`(equalTo(shaper.segmenter)))
    }

    @Test
    fun testToString_Smoother() {
        println("ToString_Smoother")
        val a = shaper.smoother.toString().parseJson().let { Smoother.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(shaper.smoother)))
    }
*/
}
