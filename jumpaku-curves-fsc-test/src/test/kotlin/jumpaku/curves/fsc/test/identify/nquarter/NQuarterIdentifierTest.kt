package jumpaku.curves.fsc.test.identify.nquarter

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifier
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifierJson
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifyResultJson
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
            val s = resourceText("FscCircular$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = resourceText("CircularResult$i.json").parseJson().let { NQuarterIdentifyResultJson.fromJson(it) }
            val a = identifier.identifyCircular(reparametrize(s))
            assertThat(a.nQuarterClass, `is`(e.nQuarterClass))
        }
    }

    @Test
    fun testIdentifyElliptic() {
        println("IdentifyElliptic")
        for (i in 0..12) {
            val s = resourceText("FscElliptic$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val e = resourceText("EllipticResult$i.json").parseJson().let { NQuarterIdentifyResultJson.fromJson(it) }
            val a = identifier.identifyElliptic(reparametrize(s))
            assertThat(a.nQuarterClass, `is`(e.nQuarterClass))
        }
    }
}

class NQuarterIdentifierJsonTest {

    val identifier = NQuarterIdentifier(25, 15)

    @Test
    fun testNQuarterIdentifierJson() {
        println("NQuarterIdentifierJson")
        val a = NQuarterIdentifierJson.toJsonStr(identifier).parseJson().let { NQuarterIdentifierJson.fromJson(it) }
        assertThat(a, `is`(equalTo(identifier)))
    }
}