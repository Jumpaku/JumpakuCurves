package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.commons.math.sum
import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class IdentifierTest {

    private fun approximateArcLength(b: Curve, nSamples: Int, subDomain: Interval): Double =
            subDomain.sample(nSamples).map(b).zipWithNext { p0, p1 -> p0.dist(p1) }.let(::sum)

    @Test
    fun testReparametrizeBSpline() {
        println("ReparametrizeBSpline")
        val b = BSpline(listOf(
                Point.xyr(0.0, 0.0, 0.0),
                Point.xyr(0.0, 600.0, 1.0),
                Point.xyr(300.0, 600.0, 2.0),
                Point.xyr(300.0, 0.0, 1.0),
                Point.xyr(600.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        val c = reparametrize(b)
        val ds = Interval(0.0, 1.0).sample(15).zipWithNext { s0, s1 ->
            approximateArcLength(b, 1000, Interval(c.toOriginal(s0), c.toOriginal(s1)))
        }
        val e = sum(ds) / ds.size
        ds.forEachIndexed { index, d ->
            assertThat("index: $index", d, `is`(closeTo(e, 1.0)))
        }
    }

    @Test
    fun testReparametrizeCS() {
        println("ReparametrizeCS")
        val R2 = sqrt(2.0)
        val cs = ConicSection(
                Point.xy(200.0, 400.0),
                Point.xy(200.0 * (1 - R2 / 2), 200.0 * (1 - R2 / 2)),
                Point.xy(400.0, 200.0),
                -R2 / 2)
        val c = reparametrize(cs)
        val ds = Interval(0.0, 1.0).sample(15).zipWithNext { s0, s1 ->
            approximateArcLength(cs, 10000, Interval(c.toOriginal(s0), c.toOriginal(s1)))
        }
        val e = sum(ds) / ds.size
        ds.forEachIndexed { index, d ->
            assertThat("index: $index", d, `is`(closeTo(e, 1.0)))
        }
    }
}