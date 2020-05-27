package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.identify.primitive.*
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class Primitive7IdentifierTest {

    val urlString = "/jumpaku/curves/fsc/test/identify/primitive/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val identifier = Primitive7Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testIdentify_L() {
        println("IdentifierPrimitive7.Identify_L")
        for (i in 0..1) {
            val fsc = resourceText("FscL$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.LineSegment
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_C() {
        println("IdentifierPrimitive7.Identify_C")
        for (i in 0..0) {
            val fsc = resourceText("FscC$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.Circle
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_CA() {
        println("IdentifierPrimitive7.Identify_CA")
        for (i in 0..2) {
            val fsc = resourceText("FscCA$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.CircularArc
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_E() {
        println("IdentifierPrimitive7.Identify_E")
        for (i in 0..0) {
            val fsc = resourceText("FscE$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.Ellipse
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_EC() {
        println("IdentifierPrimitive7.Identify_EA")
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
        println("IdentifierPrimitive7.Identify_FO")
        for (i in 0..0) {
            val fsc = resourceText("FscFO$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.OpenFreeCurve
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }

    @Test
    fun testIdentify_FC() {
        println("IdentifierPrimitive7.Identify_FC")
        for (i in 0..0) {
            val fsc = resourceText("FscFC$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = CurveClass.ClosedFreeCurve
            val s = reparametrize(fsc)
            val a = identifier.identify(s)
            assertThat(a.curveClass, `is`(e))
        }
    }
}

class Primitive7IdentifierJsonTest {

    val identifier = Primitive7Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testPrimitive7IdentifierJson() {
        println("Primitive7IdentifierJson")
        val a = Primitive7IdentifierJson.toJsonStr(identifier).parseJson().let { Primitive7IdentifierJson.fromJson(it) }
        assertThat(a, `is`(equalTo(identifier)))
    }
}
