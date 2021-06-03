package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.bezier.Bezier
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

class BezierTest {

    private val bc = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(bc.controlPoints[0], `is`(closeTo(Point.xyr(-2.0, 0.0, 1.0))))
        assertThat(bc.controlPoints[1], `is`(closeTo(Point.xyr(-1.0, 0.0, 2.0))))
        assertThat(bc.controlPoints[2], `is`(closeTo(Point.xyr(0.0, 2.0, 0.0))))
        assertThat(bc.controlPoints[3], `is`(closeTo(Point.xyr(1.0, 0.0, 2.0))))
        assertThat(bc.controlPoints[4], `is`(closeTo(Point.xyr(2.0, 0.0, 1.0))))
        assertThat(bc.controlPoints.size, `is`(5))
        assertThat(bc.degree, `is`(4))
        assertThat(bc.domain.begin, `is`(closeTo(0.0)))
        assertThat(bc.domain.end, `is`(closeTo(1.0)))
    }

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(bc.invoke(0.0), `is`(closeTo(Point.xyr(-2.0, 0.0, 1.0))))
        assertThat(bc.invoke(0.25), `is`(closeTo(Point.xyr(-1.0, 27 / 64.0, 161.0 / 128))))
        assertThat(bc.invoke(0.5), `is`(closeTo(Point.xyr(0.0, 0.75, 9.0 / 8))))
        assertThat(bc.invoke(0.75), `is`(closeTo(Point.xyr(1.0, 27 / 64.0, 161.0 / 128))))
        assertThat(bc.invoke(1.0), `is`(closeTo(Point.xyr(2.0, 0.0, 1.0))))
    }

    @Test
    fun testDifferentiate() {
        val d = bc.differentiate()
        assertThat(d.curve, `is`(closeTo(Bezier(Point.xy(4.0, 0.0), Point.xy(4.0, 8.0), Point.xy(4.0, -8.0), Point.xy(4.0, 0.0)))))

        assertThat(d.invoke(0.0), `is`(closeTo(Vector(4.0, 0.0))))
        assertThat(d.invoke(0.25), `is`(closeTo(Vector(4.0, 2.25))))
        assertThat(d.invoke(0.5), `is`(closeTo(Vector(4.0, 0.0))))
        assertThat(d.invoke(0.75), `is`(closeTo(Vector(4.0, -2.25))))
        assertThat(d.invoke(1.0), `is`(closeTo(Vector(4.0, 0.0))))
    }

    @Test
    fun testCrispTransform() {
        print("CrispTransform")
        val a = bc.transform(UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
                .andThen(Translate(Vector(1.0, 1.0, 0.0))))
        val e = Bezier(Point.xy(1.0, -3.0), Point.xy(1.0, -1.0), Point.xy(-3.0, 1.0), Point.xy(1.0, 3.0), Point.xy(1.0, 5.0))
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        assertThat(bc.toCrisp(), `is`(closeTo(
                Bezier(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)))))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        assertThat(bc.restrict(0.25, 0.5), `is`(closeTo(Bezier(
                Point.xyr(-1.0, 27 / 64.0, 161 / 128.0), Point.xyr(-3 / 4.0, 9 / 16.0, 39 / 32.0), Point.xyr(-1 / 2.0, 11 / 16.0, 37 / 32.0), Point.xyr(-1 / 4.0, 3 / 4.0, 9 / 8.0), Point.xyr(0.0, 3 / 4.0, 9 / 8.0)))))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        assertThat(bc.reverse(), `is`(closeTo(Bezier(
                Point.xyr(2.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(-1.0, 0.0, 2.0), Point.xyr(-2.0, 0.0, 1.0)))))
    }

    @Test
    fun testElevate() {
        println("Elevate")
        val instance = Bezier(Point.xr(-1.0, 0.0), Point.xr(0.0, 2.0), Point.xr(1.0, 0.0))
        val expected = Bezier(Point.xr(-1.0, 0.0), Point.xr(-1 / 3.0, 4 / 3.0), Point.xr(1 / 3.0, 4 / 3.0), Point.xr(1.0, 0.0))
        assertThat(instance.elevate(), `is`(closeTo(expected)))
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val b1 = Bezier(Point.xyr(-1.0, 2.0, 2.0), Point.xyr(1.0, 1.0, 1.0))
                .reduce()
        val e1 = Bezier(Point.xyr(0.0, 1.5, 1.5))
        assertThat(b1, `is`(closeTo(e1)))

        val b2 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(0.0, 2.0, 2.0), Point.xyr(1.0, 0.0, 0.0))
                .reduce()
        val e2 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(1.0, 0.0, 0.0))
        assertThat(b2, `is`(closeTo(e2)))

        val b3 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1.0, 0.0, 0.0)).reduce()
        val e3 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(0.0, 2.0, 2.0), Point.xyr(1.0, 0.0, 0.0))
        assertThat(b3, `is`(closeTo(e3)))

        val b4 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-0.5, 1.0, 1.0), Point.xyr(0.0, 4 / 3.0, 4 / 3.0), Point.xyr(0.5, 1.0, 1.0), Point.xyr(1.0, 0.0, 0.0))
                .reduce()
        val e4 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1.0, 0.0, 0.0))
        assertThat(b4, `is`(closeTo(e4)))

        val b5 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-0.6, 0.8, 0.8), Point.xyr(-0.3, 1.2, 1.2), Point.xyr(0.3, 1.2, 1.2), Point.xyr(0.6, 0.8, 0.8), Point.xyr(1.0, 0.0, 0.0))
                .reduce()
        val e5 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-0.5, 1.0, 1.0), Point.xyr(0.0, 4 / 3.0, 8 / 3.0), Point.xyr(0.5, 1.0, 1.0), Point.xyr(1.0, 0.0, 0.0))
        assertThat(b5, `is`(closeTo(e5)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (a0, a1) = Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
                .subdivide(0.25)
        assertThat(a0, `is`(closeTo(Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-7 / 4.0, 0.0, 5 / 4.0), Point.xyr(-3 / 2.0, 1 / 8.0, 21 / 16.0), Point.xyr(-5 / 4.0, 9 / 32.0, 83 / 64.0), Point.xyr(-1.0, 27 / 64.0, 161 / 128.0)))))
        assertThat(a1, `is`(closeTo(Bezier(
                Point.xyr(-1.0, 27 / 64.0, 322 / 256.0), Point.xyr(-1 / 4.0, 27 / 32.0, 73 / 64.0), Point.xyr(1 / 2.0, 9 / 8.0, 13 / 16.0), Point.xyr(5 / 4.0, 0.0, 7 / 4.0), Point.xyr(2.0, 0.0, 1.0)))))
    }

    @Test
    fun testExtend() {
        println("Extend")
        val extendBack = Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-7 / 4.0, 0.0, 5 / 4.0), Point.xyr(-3 / 2.0, 1 / 8.0, 21 / 16.0), Point.xyr(-5 / 4.0, 9 / 32.0, 83 / 64.0), Point.xyr(-1.0, 27 / 64.0, 161 / 128.0))
                .extend(4.0)
        assertThat(extendBack, `is`(closeTo(Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 8.0), Point.xyr(0.0, 2.0, 60.0), Point.xyr(1.0, 0.0, 434.0), Point.xyr(2.0, 0.0, 3073.0)))))

        val extendFront = Bezier(
                Point.xyr(-1.0, 27 / 64.0, 322 / 256.0), Point.xyr(-1 / 4.0, 27 / 32.0, 73 / 64.0), Point.xyr(1 / 2.0, 9 / 8.0, 13 / 16.0), Point.xyr(5 / 4.0, 0.0, 7 / 4.0), Point.xyr(2.0, 0.0, 1.0))
                .extend(-1 / 3.0)
        assertThat(extendFront, `is`(closeTo(Bezier(
                Point.xyr(-2.0, 0.0, 721 / 81.0), Point.xyr(-1.0, 0.0, 134 / 27.0), Point.xyr(0.0, 2.0, 28 / 9.0), Point.xyr(1.0, 0.0, 8 / 3.0), Point.xyr(2.0, 0.0, 1.0)))))
    }

    @Test
    fun testDecasteljau() {
        println("Decasteljau")
        val result = Bezier.decasteljau(0.25,
                listOf(Point.xyzr(1.0, 0.0, -2.0, 1.0), Point.xyzr(0.0, 3.0, 4.0, 0.0), Point.xyzr(-1.0, -1.0, 0.0, 2.0)))
        assertThat(result.size, `is`(result.size))
        assertThat(result[0], `is`(closeTo(Point.xyzr(0.75, 0.75, -0.5, 0.75))))
        assertThat(result[1], `is`(closeTo(Point.xyzr(-0.25, 2.0, 3.0, 0.5))))
    }

    @Test
    fun test_Basis() {
        print("Basis")
        assertThat(Bezier.basis(2, 0, 0.0), `is`(closeTo(1.0)))
        assertThat(Bezier.basis(2, 1, 0.0), `is`(closeTo(0.0)))
        assertThat(Bezier.basis(2, 2, 0.0), `is`(closeTo(0.0)))
        assertThat(Bezier.basis(2, 0, 0.25), `is`(closeTo(9 / 16.0)))
        assertThat(Bezier.basis(2, 1, 0.25), `is`(closeTo(6 / 16.0)))
        assertThat(Bezier.basis(2, 2, 0.25), `is`(closeTo(1 / 16.0)))
        assertThat(Bezier.basis(2, 0, 0.5), `is`(closeTo(0.25)))
        assertThat(Bezier.basis(2, 1, 0.5), `is`(closeTo(0.5)))
        assertThat(Bezier.basis(2, 2, 0.5), `is`(closeTo(0.25)))
        assertThat(Bezier.basis(2, 0, 0.75), `is`(closeTo(1 / 16.0)))
        assertThat(Bezier.basis(2, 1, 0.75), `is`(closeTo(6 / 16.0)))
        assertThat(Bezier.basis(2, 2, 0.75), `is`(closeTo(9 / 16.0)))
        assertThat(Bezier.basis(2, 0, 1.0), `is`(closeTo(0.0)))
        assertThat(Bezier.basis(2, 1, 1.0), `is`(closeTo(0.0)))
        assertThat(Bezier.basis(2, 2, 1.0), `is`(closeTo(1.0)))
    }
}

