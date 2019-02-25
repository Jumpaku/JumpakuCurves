package jumpaku.curves.fsc.test.identify.primitive.reference

import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.fsc.identify.primitive.reference.CircularGenerator
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Test

class CircularTest {

    val urlString = "/jumpaku/curves/fsc/test/identify/primitive/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = CircularGenerator(nSamples = 25)

    @Test
    fun testCircularGenerate() {
        println("CircularGenerate")
        for (i in 0..2) {
            val fsc = resourceText("FscCA$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = resourceText("ReferenceCircular$i.json").parseJson().tryMap { Reference.fromJson(it) }.orThrow()
            val s = reparametrize(fsc, 65)
            val a = generator.generate(s, t0 = s.originalCurve.domain.begin, t1 = s.originalCurve.domain.end)
            a.reparametrized.isPossible(e.reparametrized, 15).value.shouldBeGreaterThan(0.75)
        }
    }
}