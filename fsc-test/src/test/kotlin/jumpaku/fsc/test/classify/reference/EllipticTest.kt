package jumpaku.fsc.test.classify.reference

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.parseJson
import jumpaku.fsc.classify.reference.EllipticGenerator
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Test

class EllipticTest {

    val urlString = "/jumpaku/fsc/test/classify/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = EllipticGenerator(nSamples = 25)

    @Test
    fun testEllipticGenerate() {
        println("EllipticGenerate")
        val s = resourceText("ellipticFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = resourceText("ellipticPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end)
        a.isPossible(e, 15).value.shouldBeGreaterThan(0.9)
    }
}