package jumpaku.fsc.test.identify.primitive.reference

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.identify.primitive.reference.LinearGenerator
import jumpaku.fsc.identify.primitive.reference.Reference
import jumpaku.fsc.identify.primitive.reparametrize
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Test

class LinearTest {

    val urlString = "/jumpaku/fsc/test/identify/primitive/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = LinearGenerator()

    @Test
    fun testLinearGenerate() {
        println("LinearGenerate")
        for (i in 0..1) {
            val fsc = resourceText("FscL$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = resourceText("ReferenceLinear$i.json").parseJson().tryMap { Reference.fromJson(it) }.orThrow()
            val s = reparametrize(fsc, 65)
            val a = generator.generate(s, t0 = s.originalCurve.domain.begin, t1 = s.originalCurve.domain.end)
            a.reparametrized.isPossible(e.reparametrized, 15).value.shouldBeGreaterThan(0.75)
        }
    }
}