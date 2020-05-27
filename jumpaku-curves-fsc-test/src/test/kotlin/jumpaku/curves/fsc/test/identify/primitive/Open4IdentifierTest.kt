package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.Open4IdentifierJson
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.time.Duration

class Open4IdentifierTest {

    val urlString = "/jumpaku/curves/fsc/test/identify/primitive/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val identifier = Open4Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testIdentify_L() {
        println("IdentifierOpen4.Identify_L")
        for (i in 0..1) {
            val fsc = resourceText("FscL$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.LineSegment
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_CA() {
        println("IdentifierOpen4.Identify_CA")
        for (i in 0..2) {
            val fsc = resourceText("FscCA$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.CircularArc
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_EA() {
        println("IdentifierOpen4.Identify_EA")
        for (i in 0..2) {
            val fsc = resourceText("FscEA$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.EllipticArc
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_FO() {
        println("IdentifierOpen4.Identify_FO")
        for (i in 0..0) {
            val fsc = resourceText("FscFO$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.OpenFreeCurve
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }
/*
    @Test
    fun testIdentify_Time() {
        println("IdentifierOpen4.Identify_Time")
        val fsc = resourceText("FscFO0.json").parseJson().let { BSplineJson.fromJson(it) }
        val s = reparametrize(fsc)
        repeat(1000) { identifier.identify(s) }
        val b = System.nanoTime()
        assertTimeoutPreemptively(Duration.ofMillis(1500)) {
            repeat(1000) { identifier.identify(s) }
            println("    ${(System.nanoTime() - b) * 1e-9} [s]")
        }
    }
*/
}
class Open4IdentifierJsonTest {

    val identifier = Open4Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testOpen4IdentifierJson() {
        println("Open4IdentifierJson")
        val a = Open4IdentifierJson.toJsonStr(identifier).parseJson().let { Open4IdentifierJson.fromJson(it) }
        assertThat(a, `is`(equalTo(identifier)))
    }
}
