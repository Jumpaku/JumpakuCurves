package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bezier.BezierDerivative
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class BezierDerivativeTest {

    @Test
    fun testProperties() {
        println("Properties")
        val b4 = BezierDerivative(Vector(1.0, -2.0, 0.0), Vector(2.0, -1.0, 0.0), Vector(0.0, 2.0), Vector(2.0, 1.0, 0.0), Vector(1.0, 2.0, 0.0))
        assertThat(b4.controlVectors[0], `is`(closeTo(Vector(1.0, -2.0, 0.0))))
        assertThat(b4.controlVectors[1], `is`(closeTo(Vector(2.0, -1.0, 0.0))))
        assertThat(b4.controlVectors[2], `is`(closeTo(Vector(0.0, 2.0))))
        assertThat(b4.controlVectors[3], `is`(closeTo(Vector(2.0, 1.0, 0.0))))
        assertThat(b4.controlVectors[4], `is`(closeTo(Vector(1.0, 2.0, 0.0))))
        assertThat(b4.controlVectors.size, `is`(5))
        assertThat(b4.degree, `is`(4))
        assertThat(b4.domain.begin, `is`(closeTo(0.0)))
        assertThat(b4.domain.end, `is`(closeTo(1.0)))
    }

    @Test
    fun testInvoke() {
        println("Invoke")
        val b4 = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        assertThat(b4.invoke(0.0), `is`(closeTo(Vector(-2.0, 0.0))))
        assertThat(b4.invoke(0.25), `is`(closeTo(Vector(-1.0, 27 / 64.0))))
        assertThat(b4.invoke(0.5), `is`(closeTo(Vector(0.0, 0.75))))
        assertThat(b4.invoke(0.75), `is`(closeTo(Vector(1.0, 27 / 64.0))))
        assertThat(b4.invoke(1.0), `is`(closeTo(Vector(2.0, 0.0))))
    }

    @Test
    fun testDifferentiate() {
        val b = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        val d = b.differentiate()
        assertThat(d.curve, `is`(closeTo(Bezier(Point.xy(4.0, 0.0), Point.xy(4.0, 8.0), Point.xy(4.0, -8.0), Point.xy(4.0, 0.0)))))
        assertThat(d.invoke(0.0), `is`(closeTo(Vector(4.0, 0.0))))
        assertThat(d.invoke(0.25), `is`(closeTo(Vector(4.0, 2.25))))
        assertThat(d.invoke(0.5), `is`(closeTo(Vector(4.0, 0.0))))
        assertThat(d.invoke(0.75), `is`(closeTo(Vector(4.0, -2.25))))
        assertThat(d.invoke(1.0), `is`(closeTo(Vector(4.0, 0.0))))
    }

    @Test
    fun testClipout() {
        println("Clipout")
        val b = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))

        assertThat(b.clipout(0.25, 0.5).curve,
                `is`(closeTo(Bezier(Point.xy(-1.0, 27 / 64.0), Point.xy(-3 / 4.0, 9 / 16.0), Point.xy(-1 / 2.0, 11 / 16.0), Point.xy(-1 / 4.0, 3 / 4.0), Point.xy(0.0, 3 / 4.0)))))
        assertThat(b.clipout(Interval(0.25, 0.5)).curve,
                `is`(closeTo(Bezier(Point.xy(-1.0, 27 / 64.0), Point.xy(-3 / 4.0, 9 / 16.0), Point.xy(-1 / 2.0, 11 / 16.0), Point.xy(-1 / 4.0, 3 / 4.0), Point.xy(0.0, 3 / 4.0)))))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
                .reverse()
        assertThat(r.curve, `is`(closeTo(Bezier(
                Point.xy(2.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(-1.0, 0.0), Point.xy(-2.0, 0.0)))))
    }

    @Test
    fun testElevate() {
        println("Elevate")
        val instance = BezierDerivative(Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0))
                .elevate()
        val expected = Bezier(Point.xy(-1.0, 0.0), Point.xy(-1 / 3.0, 4 / 3.0), Point.xy(1 / 3.0, 4 / 3.0), Point.xy(1.0, 0.0))
        assertThat(instance.curve, `is`(closeTo(expected)))
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val b1 = BezierDerivative(Vector(-1.0, 2.0), Vector(1.0, 1.0))
                .reduce()
        val e1 = Bezier(Point.xy(0.0, 1.5))
        assertThat(b1.curve, `is`(closeTo(e1)))

        val b2 = BezierDerivative(Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0))
                .reduce()
        val e2 = Bezier(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0))
        assertThat(b2.curve, `is`(closeTo(e2)))

        val b3 = BezierDerivative(Vector(-1.0, 0.0), Vector(-1 / 3.0, 4 / 3.0), Vector(1 / 3.0, 4 / 3.0), Vector(1.0, 0.0))
                .reduce()
        val e3 = Bezier(Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0))
        assertThat(b3.curve, `is`(closeTo(e3)))

        val b4 = BezierDerivative(Vector(-1.0, 0.0), Vector(-0.5, 1.0), Vector(0.0, 4 / 3.0), Vector(0.5, 1.0), Vector(1.0, 0.0))
                .reduce()
        val e4 = Bezier(Point.xy(-1.0, 0.0), Point.xy(-1 / 3.0, 4 / 3.0), Point.xy(1 / 3.0, 4 / 3.0), Point.xy(1.0, 0.0))
        assertThat(b4.curve, `is`(closeTo(e4)))

        val b5 = BezierDerivative(Vector(-1.0, 0.0), Vector(-0.6, 0.8), Vector(-0.3, 1.2), Vector(0.3, 1.2), Vector(0.6, 0.8), Vector(1.0, 0.0))
                .reduce()
        val e5 = Bezier(Point.xy(-1.0, 0.0), Point.xy(-0.5, 1.0), Point.xy(0.0, 4 / 3.0), Point.xy(0.5, 1.0), Point.xy(1.0, 0.0))
        assertThat(b5.curve, `is`(closeTo(e5)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (a0, a1) = BezierDerivative(Vector(1.0, -2.0, 0.0), Vector(2.0, -1.0, 0.0), Vector(0.0, 0.0, 2.0), Vector(2.0, 1.0, 0.0), Vector(1.0, 2.0, 0.0))
                .subdivide(0.25)
        assertThat(a0.curve,
                `is`(closeTo(Bezier(Point.xyz(1.0, -2.0, 0.0), Point.xyz(5 / 4.0, -7 / 4.0, 0.0), Point.xyz(21 / 16.0, -3 / 2.0, 1 / 8.0), Point.xyz(83 / 64.0, -5 / 4.0, 9 / 32.0), Point.xyz(322 / 256.0, -1.0, 27 / 64.0)))))
        assertThat(a1.curve,
                `is`(closeTo(Bezier(Point.xyz(322 / 256.0, -1.0, 27 / 64.0), Point.xyz(73 / 64.0, -1 / 4.0, 27 / 32.0), Point.xyz(13 / 16.0, 1 / 2.0, 9 / 8.0), Point.xyz(7 / 4.0, 5 / 4.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))))
    }

    @Test
    fun testExtend() {
        println("Extend")
        val extendBack = BezierDerivative(
                Vector(-2.0, 0.0), Vector(-7 / 4.0, 0.0), Vector(-3 / 2.0, 1 / 8.0), Vector(-5 / 4.0, 9 / 32.0), Vector(-1.0, 27 / 64.0))
                .extend(4.0)
        assertThat(extendBack.curve,
                `is`(closeTo(Bezier(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)))))

        val extendFront = BezierDerivative(
                Vector(-1.0, 27 / 64.0), Vector(-1 / 4.0, 27 / 32.0), Vector(1 / 2.0, 9 / 8.0), Vector(5 / 4.0, 0.0), Vector(2.0, 0.0))
                .extend(-1 / 3.0)
        assertThat(extendFront.curve,
                `is`(closeTo(Bezier(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)))))
    }
}

