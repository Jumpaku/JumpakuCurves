package jumpaku.curves.fsc.test.identify.nquarter

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifier
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifyResult
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class NQuarterIdentifierTest {

    val urlString = "/jumpaku/curves/fsc/test/identify/nquarter/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()
    val identifier = NQuarterIdentifier(25, 15)

    @Test
    fun testIdentifyCircular() {
        println("IdentifyCircular")
        for (i in 0..6) {
            val s = resourceText("FscCircular$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = resourceText("CircularResult$i.json").parseJson().tryMap { NQuarterIdentifyResult.fromJson(it) }
                    .orThrow()
            val a = identifier.identifyCircular(reparametrize(s))
            assertThat(a.nQuarterClass, `is`(e.nQuarterClass))
        }
    }

    @Test
    fun testIdentifyElliptic() {
        println("IdentifyElliptic")
        for (i in 0..12) {
            val s = resourceText("FscElliptic$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = resourceText("EllipticResult$i.json").parseJson().tryMap { NQuarterIdentifyResult.fromJson(it) }
                    .orThrow()
            val a = identifier.identifyElliptic(reparametrize(s))
            assertThat(a.nQuarterClass, `is`(e.nQuarterClass))
        }
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = identifier.toString().parseJson().tryMap { NQuarterIdentifier.fromJson(it) }.orThrow()
        assertThat(a, `is`(equalTo(identifier)))
    }
}