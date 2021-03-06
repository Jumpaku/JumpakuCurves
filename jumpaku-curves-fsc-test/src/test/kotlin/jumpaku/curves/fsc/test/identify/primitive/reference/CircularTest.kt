package jumpaku.curves.fsc.test.identify.primitive.reference

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.identify.primitive.reference.CircularGenerator
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import jumpaku.curves.fsc.identify.primitive.reference.ReferenceJson
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test

class CircularTest {

    val urlString = "/jumpaku/curves/fsc/test/identify/primitive/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = CircularGenerator(nSamples = 25)

    @Test
    fun testCircularGenerate() {
        println("CircularGenerate")
        for (i in 0..2) {
            val fsc = resourceText("FscCA$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = resourceText("ReferenceCircular$i.json").parseJson().let { ReferenceJson.fromJson(it) }
            val s = reparametrize(fsc)
            val a = generator.generate(s, t0 = s.originalCurve.domain.begin, t1 = s.originalCurve.domain.end)
            assertThat(a.reparametrized.isPossible(e.reparametrized, 15).value, `is`(greaterThan(0.75)))
        }
    }
}