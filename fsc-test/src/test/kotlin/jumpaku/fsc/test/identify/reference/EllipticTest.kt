package jumpaku.fsc.test.identify.reference

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.identify.reference.EllipticGenerator
import jumpaku.fsc.identify.reference.Reference
import jumpaku.fsc.identify.reparametrize
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Test

class EllipticTest {

    val urlString = "/jumpaku/fsc/test/identify/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = EllipticGenerator(nSamples = 25)

    @Test
    fun testEllipticGenerate() {
        println("EllipticGenerate")
        for (i in 0..2) {
            val fsc = resourceText("FscEA$i.json").parseJson().tryFlatMap { BSpline.fromJson(it) }.orThrow()
            val e = resourceText("ReferenceElliptic$i.json").parseJson().tryFlatMap { Reference.fromJson(it) }.orThrow()
            val s = reparametrize(fsc, 65)
            val a = generator.generate(s, t0 = s.originalCurve.domain.begin, t1 = s.originalCurve.domain.end)
            a.reparametrized.isPossible(e.reparametrized, 15).value.shouldBeGreaterThan(0.75)
        }
    }
}