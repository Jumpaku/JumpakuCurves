package jumpaku.curves.fsc.test.fragment

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.fsc.fragment.Chunk
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ChunkTest {

    val th = Chunk.Threshold(0.4, 0.6)

    val knotVector = KnotVector.clamped(Interval.ZERO_ONE, 1, 6)
    val s0 = BSpline((0..3).map { Point.xr(it.toDouble(), it * 3.0) }, knotVector)
    val s1 = BSpline((0..3).map { Point.xr(it.toDouble(), (3.0 - it) * 3) }, knotVector)
    val s2 = BSpline((0..3).map { Point.xr(it.toDouble(), 9.0) }, knotVector)
    val s3 = BSpline((0..3).map { Point.xr(it.toDouble(), 3.0) }, knotVector)


    fun chunk(fsc: BSpline, n: Int): Chunk = Chunk(fsc.restrict(fsc.domain).sample(n))

    val c0 = chunk(s0, 4)
    val c1 = chunk(s1, 4)
    val c2 = chunk(s2, 4)
    val c3 = chunk(s3, 4)

    @Test
    fun testChunkState() {
        println("ChunkState")
        assertThat(c0.label(th), `is`(Chunk.Label.UNKNOWN))
        assertThat(c1.label(th), `is`(Chunk.Label.STAY))
        assertThat(c2.label(th), `is`(Chunk.Label.UNKNOWN))
        assertThat(c3.label(th), `is`(Chunk.Label.MOVE))
    }

    @Test
    fun testNecessity() {
        println("Necessity")
        assertThat(c0.necessity.value, `is`(closeTo(0.0)))
        assertThat(c1.necessity.value, `is`(closeTo(2 / 3.0)))
        assertThat(c2.necessity.value, `is`(closeTo(1 / 3.0)))
        assertThat(c3.necessity.value, `is`(closeTo(0.0)))
    }

    @Test
    fun testPossibility() {
        println("Possibility")
        assertThat(c0.possibility.value, `is`(closeTo(2 / 3.0)))
        assertThat(c1.possibility.value, `is`(closeTo(2 / 3.0)))
        assertThat(c2.possibility.value, `is`(closeTo(2.5 / 3)))
        assertThat(c3.possibility.value, `is`(closeTo(0.5)))
    }

    @Test
    fun testBeginParam() {
        println("Necessity")
        assertThat(c0.beginParam, `is`(closeTo(0.0)))
        assertThat(c1.beginParam, `is`(closeTo(0.0)))
        assertThat(c2.beginParam, `is`(closeTo(0.0)))
        assertThat(c3.beginParam, `is`(closeTo(0.0)))
    }

    @Test
    fun testEndParam() {
        println("Possibility")
        assertThat(c0.endParam, `is`(closeTo(1.0)))
        assertThat(c1.endParam, `is`(closeTo(1.0)))
        assertThat(c2.endParam, `is`(closeTo(1.0)))
        assertThat(c3.endParam, `is`(closeTo(1.0)))
    }

    @Test
    fun testInterval() {
        println("Interval")
        assertThat(c0.interval, `is`(closeTo(Interval.ZERO_ONE)))
        assertThat(c1.interval, `is`(closeTo(Interval.ZERO_ONE)))
        assertThat(c2.interval, `is`(closeTo(Interval.ZERO_ONE)))
        assertThat(c3.interval, `is`(closeTo(Interval.ZERO_ONE)))
    }
}