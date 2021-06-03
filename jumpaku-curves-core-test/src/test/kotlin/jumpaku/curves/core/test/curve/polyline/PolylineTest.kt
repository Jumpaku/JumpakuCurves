package jumpaku.curves.core.test.curve.polyline

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PolylineTest {

    val pl = Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(pl.points[0], `is`(closeTo(Point.xyr(-1.0, 1.0, 2.0))))
        assertThat(pl.points[1], `is`(closeTo(Point.xyr(1.0, 1.0, 1.0))))
        assertThat(pl.points[2], `is`(closeTo(Point.xyr(1.0, -3.0, 3.0))))
        assertThat(pl.points[3], `is`(closeTo(Point.xyzr(1.0, -3.0, 1.5, 2.0))))
        assertThat(pl.domain.begin, `is`(closeTo(0.0)))
        assertThat(pl.domain.end, `is`(closeTo(7.5)))
    }

    @Test
    fun testInvoke() {
        println("Invoke")
        val p = Polyline.byArcLength(Point.xyr(1.0, 1.0, 2.0), Point.xyr(-1.0, -1.0, 1.0), Point.xyzr(-1.0, -1.0, 1.0, 0.0))
        assertThat(p.invoke(0.0), `is`(closeTo(Point.xyzr(1.0, 1.0, 0.0, 2.0))))
        assertThat(p.invoke(Math.sqrt(2.0)), `is`(closeTo(Point.xyzr(0.0, 0.0, 0.0, 1.5))))
        assertThat(p.invoke(2 * Math.sqrt(2.0)), `is`(closeTo(Point.xyzr(-1.0, -1.0, 0.0, 1.0))))
        assertThat(p.invoke(2 * Math.sqrt(2.0) + 0.5), `is`(closeTo(Point.xyzr(-1.0, -1.0, 0.5, 0.5))))
        assertThat(p.invoke(2 * Math.sqrt(2.0) + 1), `is`(closeTo(Point.xyzr(-1.0, -1.0, 1.0, 0.0))))
    }

    @Test
    fun testInvokeAll() {
        println("InvokeAll")
        val ps = pl.invoke(Sampler(6))
        assertThat(ps.size, `is`(6))
        assertThat(ps[0], `is`(closeTo(Point.xyzr(-1.0, 1.0, 0.0, 2.0))))
        assertThat(ps[1], `is`(closeTo(Point.xyzr(0.5, 1.0, 0.0, 1.25))))
        assertThat(ps[2], `is`(closeTo(Point.xyzr(1.0, 0.0, 0.0, 1.5))))
        assertThat(ps[3], `is`(closeTo(Point.xyzr(1.0, -1.5, 0.0, 2.25))))
        assertThat(ps[4], `is`(closeTo(Point.xyzr(1.0, -3.0, 0.0, 3.0))))
        assertThat(ps[5], `is`(closeTo(Point.xyzr(1.0, -3.0, 1.5, 2.0))))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = pl.transform(UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
                .andThen(Translate(Vector(1.0, 1.0))))
        val e = Polyline.byArcLength(Point.xy(-1.0, -1.0), Point.xy(-1.0, 3.0), Point.xy(7.0, 3.0), Point.xyz(7.0, 3.0, 3.0))
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        assertThat(pl.toCrisp(), `is`(closeTo(
                Polyline.byArcLength(Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, -3.0), Point.xyz(1.0, -3.0, 1.5)))))

    }

    @Test
    fun testReverse() {
        println("Reverse")
        assertThat(pl.reverse(), `is`(closeTo(
                Polyline.byArcLength(Point.xyzr(1.0, -3.0, 1.5, 2.0), Point.xyr(1.0, -3.0, 3.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(-1.0, 1.0, 2.0)))))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        assertThat(pl.restrict(3.0, 4.5), `is`(closeTo(Polyline(listOf(ParamPoint(Point.xyr(1.0, 0.0, 1.5), 3.0), ParamPoint(Point.xyr(1.0, -1.5, 2.25), 4.5))))))
        assertThat(pl.restrict(Interval(3.0, 4.5)), `is`(closeTo(Polyline(listOf(ParamPoint(Point.xyr(1.0, 0.0, 1.5), 3.0), ParamPoint(Point.xyr(1.0, -1.5, 2.25), 4.5))))))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val ps0 = pl.subdivide(4.5)
        assertThat(ps0.first, `is`(closeTo(Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -1.5, 2.25)))))
        assertThat(ps0.second, `is`(closeTo(Polyline.byArcLength(Point.xyr(1.0, -1.5, 2.25), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0)))))

        val ps1 = pl.subdivide(0.0)
        assertThat(ps1.first, `is`(closeTo(Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0)))))
        assertThat(ps1.second, `is`(closeTo(pl)))

        val ps2 = pl.subdivide(7.5)
        assertThat(ps2.first, `is`(closeTo(pl)))
        assertThat(ps2.second, `is`(closeTo(Polyline(listOf(ParamPoint(Point.xyzr(1.0, -3.0, 1.5, 2.0), 7.5))))))

        val ps3 = pl.subdivide(2.0)
        assertThat(ps3.first, `is`(closeTo(Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0)))))
        assertThat(ps3.second, `is`(closeTo(Polyline(listOf(
                ParamPoint(Point.xyr(1.0, 1.0, 1.0), 2.0),
                ParamPoint(Point.xyr(1.0, -3.0, 3.0), 6.0),
                ParamPoint(Point.xyzr(1.0, -3.0, 1.5, 2.0), 7.5))))))
    }

    @Test
    fun testByIndices() {
        println("ByIndices")
        val byIndices = Polyline.byIndices(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))
        assertThat(byIndices.points[0], `is`(closeTo(Point.xyr(-1.0, 1.0, 2.0))))
        assertThat(byIndices.points[1], `is`(closeTo(Point.xyr(1.0, 1.0, 1.0))))
        assertThat(byIndices.points[2], `is`(closeTo(Point.xyr(1.0, -3.0, 3.0))))
        assertThat(byIndices.points[3], `is`(closeTo(Point.xyzr(1.0, -3.0, 1.5, 2.0))))
        assertThat(byIndices.domain.begin, `is`(closeTo(0.0)))
        assertThat(byIndices.domain.end, `is`(closeTo(3.0)))
    }
}

