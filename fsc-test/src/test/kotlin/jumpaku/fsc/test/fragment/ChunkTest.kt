package jumpaku.fsc.test.fragment

import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.fsc.fragment.Chunk
import jumpaku.fsc.fragment.TruthValueThreshold
import jumpaku.fsc.fragment.chunk
import org.amshove.kluent.shouldBe
import org.junit.Test

class ChunkTest {

    val urlString = "/jumpaku/fsc/test/fragment/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    private val fsc = resourceText("FragmenterTestFsc0.json").parseJson().flatMap { BSpline.fromJson(it) }.get()

    private val eps = 1.0e-5
    private val threshold = TruthValueThreshold(0.4, 0.6)

    @Test
    fun testState() {
        println("State")
        val c1 = chunk(fsc, Interval(106535.12613733625, 106535.20095651352), 4)
        c1.state(threshold).shouldBe(Chunk.State.MOVE)
        val c2 = chunk(fsc, Interval(106535.45035377113, 106535.52517294842), 4)
        c2.state(threshold).shouldBe(Chunk.State.STAY)
        val c3 = chunk(fsc, Interval(106535.55011267416, 106535.62493185145), 4)
        c3.state(threshold).shouldBe(Chunk.State.UNKNOWN)
    }

    @Test
    fun testNecessity() {
        val c = chunk(fsc, Interval(106535.55011267416, 106535.62493185145), 4)
        c.necessity.value.shouldBeCloseTo(0.042337862693934136, eps)
    }

    @Test
    fun testPossibility() {
        val c = chunk(fsc, Interval(106535.55011267416, 106535.62493185145), 4)
        c.possibility.value.shouldBeCloseTo(0.8946264226651022, eps)
    }

}