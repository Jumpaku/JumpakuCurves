package jumpaku.curves.fsc.test.identify.primitive.reference

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.identify.primitive.reference.EllipticGenerator
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import jumpaku.curves.fsc.identify.primitive.reference.ReferenceJson
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class EllipticTest {

    val urlString = "/jumpaku/curves/fsc/test/identify/primitive/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = EllipticGenerator(nSamples = 25)

    @Test
    fun testEllipticGenerate() {
        println("EllipticGenerate")
        for (i in 0..2) {
            val fsc = resourceText("FscEA$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = resourceText("ReferenceElliptic$i.json").parseJson().let { ReferenceJson.fromJson(it) }
            val s = reparametrize(fsc)
            val a = generator.generate(s, t0 = s.originalCurve.domain.begin, t1 = s.originalCurve.domain.end)
            Assert.assertThat(a.reparametrized.isPossible(e.reparametrized, 15).value, Matchers.`is`(Matchers.greaterThan(0.75)))
        }
    }
}