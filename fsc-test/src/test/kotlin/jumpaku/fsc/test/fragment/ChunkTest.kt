package jumpaku.fsc.test.fragment

import jumpaku.core.geom.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.fsc.fragment.Chunk
import jumpaku.fsc.fragment.TruthValueThreshold
import jumpaku.fsc.fragment.chunk
import org.amshove.kluent.shouldBe
import org.junit.Test

class ChunkTest {

    private val threshold = TruthValueThreshold(0.4, 0.6)

    val s0 = BSpline((0..3).map { Point.xr(it.toDouble(), it*3.0) }, KnotVector.uniform(Interval.ZERO_ONE, 1, 6))
    val s1 = BSpline((0..3).map { Point.xr(it.toDouble(), (3.0 - it)*3) }, KnotVector.uniform(Interval.ZERO_ONE, 1, 6))
    val s2 = BSpline((0..3).map { Point.xr(it.toDouble(), 9.0) }, KnotVector.uniform(Interval.ZERO_ONE, 1, 6))
    val s3 = BSpline((0..3).map { Point.xr(it.toDouble(), 3.0) }, KnotVector.uniform(Interval.ZERO_ONE, 1, 6))

    val c0 = chunk(s0, s0.domain, 4)
    val c1 = chunk(s1, s1.domain, 4)
    val c2 = chunk(s2, s2.domain, 4)
    val c3 = chunk(s3, s3.domain, 4)

    @Test
    fun testChunkCreate() {
        println("ChunkState")
        c0.state(threshold).shouldBe(Chunk.State.UNKNOWN)
        c1.state(threshold).shouldBe(Chunk.State.STAY)
        c2.state(threshold).shouldBe(Chunk.State.UNKNOWN)
        c3.state(threshold).shouldBe(Chunk.State.MOVE)
    }

    @Test
    fun testNecessity() {
        println("Necessity")
        c0.necessity.value.shouldBeCloseTo(0.0)
        c1.necessity.value.shouldBeCloseTo(2/3.0)
        c2.necessity.value.shouldBeCloseTo(1/3.0)
        c3.necessity.value.shouldBeCloseTo(0.0)
    }

    @Test
    fun testPossibility() {
        println("Possibility")
        c0.possibility.value.shouldBeCloseTo(2/3.0)
        c1.possibility.value.shouldBeCloseTo(2/3.0)
        c2.possibility.value.shouldBeCloseTo(2.5/3)
        c3.possibility.value.shouldBeCloseTo(0.5)
    }

}