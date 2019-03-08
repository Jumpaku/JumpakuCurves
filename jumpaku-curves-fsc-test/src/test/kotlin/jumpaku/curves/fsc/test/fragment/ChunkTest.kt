package jumpaku.curves.fsc.test.fragment

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.closeTo
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.fragment.chunk
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ChunkTest {

    private val threshold = Fragmenter.Threshold(0.4, 0.6)

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
        assertThat(c0.state(threshold), `is`(Chunk.State.UNKNOWN))
        assertThat(c1.state(threshold), `is`(Chunk.State.STAY))
        assertThat(c2.state(threshold), `is`(Chunk.State.UNKNOWN))
        assertThat(c3.state(threshold), `is`(Chunk.State.MOVE))
    }

    @Test
    fun testNecessity() {
        println("Necessity")
        assertThat(c0.necessity.value, `is`(closeTo(0.0)))
        assertThat(c1.necessity.value, `is`(closeTo(2/3.0)))
        assertThat(c2.necessity.value, `is`(closeTo(1/3.0)))
        assertThat(c3.necessity.value, `is`(closeTo(0.0)))
    }

    @Test
    fun testPossibility() {
        println("Possibility")
        assertThat(c0.possibility.value, `is`(closeTo(2/3.0)))
        assertThat(c1.possibility.value, `is`(closeTo(2/3.0)))
        assertThat(c2.possibility.value, `is`(closeTo(2.5/3)))
        assertThat(c3.possibility.value, `is`(closeTo(0.5)))
    }

}