package jumpaku.fsc.test.identify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.identify.Open4Identifier
import jumpaku.fsc.identify.reparametrize
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.jupiter.api.Assertions
import java.time.Duration

class Open4IdentifierTest {

    val urlString = "/jumpaku/fsc/test/identify/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val identifier = Open4Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testIdentify_L() {
        println("IdentifierOpen4.Identify_L")
        for (i in 0..1) {
            val fsc = resourceText("FscL$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val e = CurveClass.LineSegment
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_CA() {
        println("IdentifierOpen4.Identify_CA")
        for (i in 0..2) {
            val fsc = resourceText("FscCA$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val e = CurveClass.CircularArc
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_EA() {
        println("IdentifierOpen4.Identify_EA")
        for (i in 0..2) {
            val fsc = resourceText("FscEA$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val e = CurveClass.EllipticArc
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_FO() {
        println("IdentifierOpen4.Identify_FO")
        for (i in 0..0) {
            val fsc = resourceText("FscFO$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val e = CurveClass.OpenFreeCurve
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_Time() {
        println("IdentifierOpen4.Identify_Time")
        val fsc = resourceText("FscFO0.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val s = reparametrize(fsc, 65)
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(1500)) {
            repeat(1000) { identifier.identify(s) }
        }
    }
}