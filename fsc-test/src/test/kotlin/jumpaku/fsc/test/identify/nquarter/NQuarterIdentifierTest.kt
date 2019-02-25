package jumpaku.fsc.test.identify.nquarter

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.identify.nquarter.NQuarterIdentifier
import jumpaku.fsc.identify.nquarter.NQuarterIdentifyResult
import jumpaku.fsc.identify.reparametrize
import org.junit.Test
import java.nio.file.Paths

class NQuarterIdentifierTest {

    val path = Paths.get("./src/test/resources/jumpaku/fsc/test/identify/nquarter")
    val nQuarter = NQuarterIdentifier(25, 15)

    @Test
    fun testIdentifyCircular() {
        println("IdentifyCircular")
        for (i in 0..6) {
            val s = path.resolve("FscCircular$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = path.resolve("CircularResult$i.json").parseJson().tryMap { NQuarterIdentifyResult.fromJson(it) }
                .orThrow()
            val a = nQuarter.identifyCircular(reparametrize(s))
            a.shouldBeCloseTo(e, 1e-6)
        }
    }

    @Test
    fun testIdentifyElliptic() {
        println("IdentifyElliptic")
        for (i in 0..12) {
            val s = path.resolve("FscElliptic$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val e = path.resolve("EllipticResult$i.json").parseJson().tryMap { NQuarterIdentifyResult.fromJson(it) }
                .orThrow()
            val a = nQuarter.identifyElliptic(reparametrize(s))
            a.shouldBeCloseTo(e, 1e-6)
        }
    }
}