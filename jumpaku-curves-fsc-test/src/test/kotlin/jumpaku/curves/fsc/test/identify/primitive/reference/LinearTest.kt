package jumpaku.curves.fsc.test.identify.primitive.reference

import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.identify.primitive.reference.LinearGenerator
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class LinearTest {

    val urlString = "/jumpaku/curves/fsc/test/identify/primitive/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = LinearGenerator()

    @Test
    fun testLinearGenerate() {
        println("LinearGenerate")
        for (i in 0..1) {
            val fsc = resourceText("FscL$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = resourceText("ReferenceLinear$i.json").parseJson().tryMap { Reference.fromJson(it) }.orThrow()
            val s = reparametrize(fsc)
            val a = generator.generate(s, t0 = s.originalCurve.domain.begin, t1 = s.originalCurve.domain.end)
            Assert.assertThat(a.reparametrized.isPossible(e.reparametrized, 15).value, Matchers.`is`(Matchers.greaterThan(0.75)))
        }
    }
}