package jumpaku.fsc.test.identify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.identify.IdentifyResult
import jumpaku.fsc.identify.Primitive7Identifier
import jumpaku.fsc.identify.reparametrize
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Test

class Primitive7IdentifierTest {

    val urlString = "/jumpaku/fsc/test/identify/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val identifier = Primitive7Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testIdentify_L() {
        println("IdentifierPrimitive7.Identify_L")
        for (i in 0..1) {
            val fsc = resourceText("FscL$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = CurveClass.LineSegment
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_C() {
        println("IdentifierPrimitive7.Identify_C")
        for (i in 0..0) {
            val fsc = resourceText("FscC$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = CurveClass.Circle
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_CA() {
        println("IdentifierPrimitive7.Identify_CA")
        for (i in 0..2) {
            val fsc = resourceText("FscCA$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = CurveClass.CircularArc
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_E() {
        println("IdentifierPrimitive7.Identify_E")
        for (i in 0..0) {
            val fsc = resourceText("FscE$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = CurveClass.Ellipse
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_EC() {
        println("IdentifierPrimitive7.Identify_EA")
        for (i in 0..2) {
            val fsc = resourceText("FscEA$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = CurveClass.EllipticArc
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_FO() {
        println("IdentifierPrimitive7.Identify_FO")
        for (i in 0..0) {
            val fsc = resourceText("FscFO$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = CurveClass.OpenFreeCurve
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }

    @Test
    fun testIdentify_FC() {
        println("IdentifierPrimitive7.Identify_FC")
        for (i in 0..0) {
            val fsc = resourceText("FscFC$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = CurveClass.ClosedFreeCurve
            val s = reparametrize(fsc, 65)
            val a = identifier.identify(s)
            a.curveClass.shouldEqual(e)
        }
    }
}