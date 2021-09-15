package jumpaku.curves.core.test.geom

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.geom.*
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PointTest {

    val f = Point.xyzr(1.0, -2.0, 3.0, 2.0)
    val c = Point.xyz(1.0, -2.0, 3.0)

    @Test
    fun testCreate() {
        println("Create")
        assertThat(Point.xr(1.0, 2.0).x, `is`(closeTo(1.0)))
        assertThat(Point.xr(1.0, 2.0).y, `is`(closeTo(0.0)))
        assertThat(Point.xr(1.0, 2.0).z, `is`(closeTo(0.0)))
        assertThat(Point.xr(1.0, 2.0).r, `is`(closeTo(2.0)))

        assertThat(Point.xyr(1.0, -2.0, 2.0).x, `is`(closeTo(1.0)))
        assertThat(Point.xyr(1.0, -2.0, 2.0).y, `is`(closeTo(-2.0)))
        assertThat(Point.xyr(1.0, -2.0, 2.0).z, `is`(closeTo(0.0)))
        assertThat(Point.xyr(1.0, -2.0, 2.0).r, `is`(closeTo(2.0)))

        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).x, `is`(closeTo(1.0)))
        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).y, `is`(closeTo(-2.0)))
        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).z, `is`(closeTo(3.0)))
        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).r, `is`(closeTo(2.0)))

        assertThat(Point.x(1.0).x, `is`(closeTo(1.0)))
        assertThat(Point.x(1.0).y, `is`(closeTo(0.0)))
        assertThat(Point.x(1.0).z, `is`(closeTo(0.0)))
        assertThat(Point.x(1.0).r, `is`(closeTo(0.0)))

        assertThat(Point.xy(1.0, -2.0).x, `is`(closeTo(1.0)))
        assertThat(Point.xy(1.0, -2.0).y, `is`(closeTo(-2.0)))
        assertThat(Point.xy(1.0, -2.0).z, `is`(closeTo(0.0)))
        assertThat(Point.xy(1.0, -2.0).r, `is`(closeTo(0.0)))

        assertThat(Point.xyz(1.0, -2.0, 3.0).x, `is`(closeTo(1.0)))
        assertThat(Point.xyz(1.0, -2.0, 3.0).y, `is`(closeTo(-2.0)))
        assertThat(Point.xyz(1.0, -2.0, 3.0).z, `is`(closeTo(3.0)))
        assertThat(Point.xyz(1.0, -2.0, 3.0).r, `is`(closeTo(0.0)))
    }

    @Test
    fun testPossibility() {
        println("Possibility")
        val p0 = Point.xyr(0.0, 1.0, 1.0)
        val p1 = Point.xyr(0.0, 1.0, 2.0)
        val p2 = Point.xyr(1.0, 1.0, 0.5)
        val p3 = Point.xyr(1.0, 1.0, 2.0)
        val p4 = Point.xyr(2.0, 1.0, 1.0)
        val p5 = Point.xyr(2.0, 1.0, 4.0)
        val p6 = Point.xyr(3.0, 1.0, 1.0)
        val p7 = Point.xyr(3.0, 1.0, 8.0)
        val p8 = Point.xy(0.0, 1.0)
        val p9 = Point.xy(1.0, 1.0)
        val p10 = Point.xy(2.0, 1.0)
        val p11 = Point.xy(3.0, 1.0)

        val f = Point.xyr(0.0, 1.0, 2.0)
        assertThat(f.isPossible(p0).value, `is`(closeTo(1.0)))
        assertThat(f.isPossible(p1).value, `is`(closeTo(1.0)))
        assertThat(f.isPossible(p2).value, `is`(closeTo(3.0 / 5)))
        assertThat(f.isPossible(p3).value, `is`(closeTo(0.75)))
        assertThat(f.isPossible(p4).value, `is`(closeTo(1.0 / 3)))
        assertThat(f.isPossible(p5).value, `is`(closeTo(2.0 / 3)))
        assertThat(f.isPossible(p6).value, `is`(closeTo(0.0)))
        assertThat(f.isPossible(p7).value, `is`(closeTo(0.7)))
        assertThat(f.isPossible(p8).value, `is`(closeTo(1.0)))
        assertThat(f.isPossible(p9).value, `is`(closeTo(0.5)))
        assertThat(f.isPossible(p10).value, `is`(closeTo(0.0)))
        assertThat(f.isPossible(p11).value, `is`(closeTo(0.0)))

        val c = Point.xy(0.0, 1.0)
        assertThat(c.isPossible(p0).value, `is`(closeTo(1.0)))
        assertThat(c.isPossible(p1).value, `is`(closeTo(1.0)))
        assertThat(c.isPossible(p2).value, `is`(closeTo(0.0)))
        assertThat(c.isPossible(p3).value, `is`(closeTo(0.5)))
        assertThat(c.isPossible(p4).value, `is`(closeTo(0.0)))
        assertThat(c.isPossible(p5).value, `is`(closeTo(0.5)))
        assertThat(c.isPossible(p6).value, `is`(closeTo(0.0)))
        assertThat(c.isPossible(p7).value, `is`(closeTo(5.0 / 8)))
        assertThat(c.isPossible(p8).value, `is`(closeTo(1.0)))
        assertThat(c.isPossible(p9).value, `is`(closeTo(0.0)))
        assertThat(c.isPossible(p10).value, `is`(closeTo(0.0)))
        assertThat(c.isPossible(p11).value, `is`(closeTo(0.0)))
    }

    @Test
    fun testNecessity() {
        println("Necessity")
        val p0 = Point.xyr(0.0, 1.0, 1.0)
        val p1 = Point.xyr(0.0, 1.0, 2.0)
        val p2 = Point.xyr(1.0, 1.0, 0.5)
        val p3 = Point.xyr(1.0, 1.0, 2.0)
        val p4 = Point.xyr(2.0, 1.0, 1.0)
        val p5 = Point.xyr(2.0, 1.0, 4.0)
        val p6 = Point.xyr(3.0, 1.0, 1.0)
        val p7 = Point.xyr(3.0, 1.0, 8.0)
        val p8 = Point.xy(0.0, 1.0)
        val p9 = Point.xy(1.0, 1.0)
        val p10 = Point.xy(2.0, 1.0)
        val p11 = Point.xy(3.0, 1.0)

        val f = Point.xyr(0.0, 1.0, 2.0)
        assertThat(f.isNecessary(p0).value, `is`(closeTo(1.0 / 3)))
        assertThat(f.isNecessary(p1).value, `is`(closeTo(0.5)))
        assertThat(f.isNecessary(p2).value, `is`(closeTo(0.0)))
        assertThat(f.isNecessary(p3).value, `is`(closeTo(0.25)))
        assertThat(f.isNecessary(p4).value, `is`(closeTo(0.0)))
        assertThat(f.isNecessary(p5).value, `is`(closeTo(1.0 / 3)))
        assertThat(f.isNecessary(p6).value, `is`(closeTo(0.0)))
        assertThat(f.isNecessary(p7).value, `is`(closeTo(0.5)))
        assertThat(f.isNecessary(p8).value, `is`(closeTo(0.0)))
        assertThat(f.isNecessary(p9).value, `is`(closeTo(0.0)))
        assertThat(f.isNecessary(p10).value, `is`(closeTo(0.0)))
        assertThat(f.isNecessary(p11).value, `is`(closeTo(0.0)))

        val c = Point.xy(0.0, 1.0)
        assertThat(c.isNecessary(p0).value, `is`(closeTo(1.0)))
        assertThat(c.isNecessary(p1).value, `is`(closeTo(1.0)))
        assertThat(c.isNecessary(p2).value, `is`(closeTo(0.0)))
        assertThat(c.isNecessary(p3).value, `is`(closeTo(0.5)))
        assertThat(c.isNecessary(p4).value, `is`(closeTo(0.0)))
        assertThat(c.isNecessary(p5).value, `is`(closeTo(0.5)))
        assertThat(c.isNecessary(p6).value, `is`(closeTo(0.0)))
        assertThat(c.isNecessary(p7).value, `is`(closeTo(5.0 / 8)))
        assertThat(c.isNecessary(p8).value, `is`(closeTo(1.0)))
        assertThat(c.isNecessary(p9).value, `is`(closeTo(0.0)))
        assertThat(c.isNecessary(p10).value, `is`(closeTo(0.0)))
        assertThat(c.isNecessary(p11).value, `is`(closeTo(0.0)))
    }

    @Test
    fun testLerp() {
        println("Lerp")
        val c0 = Point.x(1.0)
        val c1 = Point.x(2.0)
        val f0 = Point.xr(1.0, 10.0)
        val f1 = Point.xr(2.0, 10.0)
        val f2 = Point.xr(2.0, 20.0)

        // lerp(terms)
        assertThat(c0.lerp(0.3 to c1), `is`(closeTo(Point.xr(1.3, 0.0))))
        assertThat(c0.lerp(-1.0 to c1), `is`(closeTo(Point.xr(0.0, 0.0))))
        assertThat(c0.lerp(2.0 to c1), `is`(closeTo(Point.xr(3.0, 0.0))))
        assertThat(c0.lerp(0.0 to c1), `is`(closeTo(Point.xr(1.0, 0.0))))
        assertThat(c0.lerp(1.0 to c1), `is`(closeTo(Point.xr(2.0, 0.0))))

        assertThat(f0.lerp(0.3 to c1), `is`(closeTo(Point.xr(1.3, 7.0))))
        assertThat(f0.lerp(-1.0 to c1), `is`(closeTo(Point.xr(0.0, 20.0))))
        assertThat(f0.lerp(2.0 to c1), `is`(closeTo(Point.xr(3.0, 10.0))))
        assertThat(f0.lerp(0.0 to c1), `is`(closeTo(Point.xr(1.0, 10.0))))
        assertThat(f0.lerp(1.0 to c1), `is`(closeTo(Point.xr(2.0, 0.0))))

        assertThat(c0.lerp(0.3 to f1), `is`(closeTo(Point.xr(1.3, 3.0))))
        assertThat(c0.lerp(-1.0 to f1), `is`(closeTo(Point.xr(0.0, 10.0))))
        assertThat(c0.lerp(2.0 to f1), `is`(closeTo(Point.xr(3.0, 20.0))))
        assertThat(c0.lerp(0.0 to f1), `is`(closeTo(Point.xr(1.0, 0.0))))
        assertThat(c0.lerp(1.0 to f1), `is`(closeTo(Point.xr(2.0, 10.0))))

        assertThat(f0.lerp(0.3 to f2), `is`(closeTo(Point.xr(1.3, 13.0))))
        assertThat(f0.lerp(-1.0 to f2), `is`(closeTo(Point.xr(0.0, 40.0))))
        assertThat(f0.lerp(2.0 to f2), `is`(closeTo(Point.xr(3.0, 50.0))))
        assertThat(f0.lerp(0.0 to f2), `is`(closeTo(Point.xr(1.0, 10.0))))
        assertThat(f0.lerp(1.0 to f2), `is`(closeTo(Point.xr(2.0, 20.0))))

        // lerp(t, p)
        assertThat(c0.lerp(0.3, c1), `is`(closeTo(Point.xr(1.3, 0.0))))
        assertThat(c0.lerp(-1.0, c1), `is`(closeTo(Point.xr(0.0, 0.0))))
        assertThat(c0.lerp(2.0, c1), `is`(closeTo(Point.xr(3.0, 0.0))))
        assertThat(c0.lerp(0.0, c1), `is`(closeTo(Point.xr(1.0, 0.0))))
        assertThat(c0.lerp(1.0, c1), `is`(closeTo(Point.xr(2.0, 0.0))))

        assertThat(f0.lerp(0.3, c1), `is`(closeTo(Point.xr(1.3, 7.0))))
        assertThat(f0.lerp(-1.0, c1), `is`(closeTo(Point.xr(0.0, 20.0))))
        assertThat(f0.lerp(2.0, c1), `is`(closeTo(Point.xr(3.0, 10.0))))
        assertThat(f0.lerp(0.0, c1), `is`(closeTo(Point.xr(1.0, 10.0))))
        assertThat(f0.lerp(1.0, c1), `is`(closeTo(Point.xr(2.0, 0.0))))

        assertThat(c0.lerp(0.3, f1), `is`(closeTo(Point.xr(1.3, 3.0))))
        assertThat(c0.lerp(-1.0, f1), `is`(closeTo(Point.xr(0.0, 10.0))))
        assertThat(c0.lerp(2.0, f1), `is`(closeTo(Point.xr(3.0, 20.0))))
        assertThat(c0.lerp(0.0, f1), `is`(closeTo(Point.xr(1.0, 0.0))))
        assertThat(c0.lerp(1.0, f1), `is`(closeTo(Point.xr(2.0, 10.0))))

        assertThat(f0.lerp(0.3, f2), `is`(closeTo(Point.xr(1.3, 13.0))))
        assertThat(f0.lerp(-1.0, f2), `is`(closeTo(Point.xr(0.0, 40.0))))
        assertThat(f0.lerp(2.0, f2), `is`(closeTo(Point.xr(3.0, 50.0))))
        assertThat(f0.lerp(0.0, f2), `is`(closeTo(Point.xr(1.0, 10.0))))
        assertThat(f0.lerp(1.0, f2), `is`(closeTo(Point.xr(2.0, 20.0))))
    }

    @Test
    fun testToVector() {
        println("ToVector")
        assertThat(f.toVector(), `is`(closeTo(Vector(1.0, -2.0, 3.0))))
        assertThat(c.toVector(), `is`(closeTo(Vector(1.0, -2.0, 3.0))))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        assertThat(f.toCrisp(), `is`(closeTo(Point.xyz(1.0, -2.0, 3.0))))
        assertThat(c.toCrisp(), `is`(closeTo(Point.xyz(1.0, -2.0, 3.0))))
    }

    @Test
    fun testToArray() {
        println("ToArray")
        val a = f.toDoubleArray()
        assertThat(a[0], `is`(closeTo(1.0)))
        assertThat(a[1], `is`(closeTo(-2.0)))
        assertThat(a[2], `is`(closeTo(3.0)))
    }

    @Test
    fun testPlus() {
        println("Plus")
        val p0 = Point.xyz(-1.0, -2.0, 4.0) + Vector(1.0, -2.0, 3.0)
        assertThat(p0, `is`(closeTo(Point.xyz(0.0, -4.0, 7.0))))
        val p1 = Point.xyz(-1.0, -2.0, 4.0) - Vector(1.0, -2.0, 3.0)
        assertThat(p1, `is`(closeTo(Point.xyz(-2.0, 0.0, 1.0))))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val p = Point.xyz(-1.0, -2.0, 4.0) - Point.xyz(1.0, -2.0, 3.0)
        assertThat(p, `is`(closeTo(Vector(-2.0, 0.0, 1.0))))
    }

    @Test
    fun testDist() {
        println("Dist")
        val dp = Point.xyz(-1.0, -2.0, 4.0).dist(Point.xyz(1.0, -2.0, 3.0))
        assertThat(dp, `is`(closeTo(FastMath.sqrt(5.0))))
        val dl = Point.xyz(1.0, -1.0, 0.0).dist(Line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        assertThat(dl, `is`(closeTo(12 / 5.0)))
        val dplane = Point.xyz(1.0, -1.0, -3.0).dist(Plane(Point.xyz(1.0, -1.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        assertThat(dplane, `is`(closeTo(3.0)))
    }

    @Test
    fun testDistSquare() {
        println("DistSquare")
        val dp = Point.xyz(-1.0, -2.0, 4.0).distSquare(Point.xyz(1.0, -2.0, 3.0))
        assertThat(dp, `is`(closeTo(5.0)))
        val dl = Point.xyz(1.0, -1.0, 0.0).distSquare(Line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        assertThat(dl, `is`(closeTo(144 / 25.0)))
        val dplane = Point.xyz(1.0, -1.0, -3.0).distSquare(Plane(Point.xyz(2.0, -2.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        assertThat(dplane, `is`(closeTo(9.0)))
    }

    @Test
    fun testProjectTo() {
        println("ProjectTo")
        val pl = Point.xyz(1.0, -1.0, 0.0).projectTo(Line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 3.0, 0.0)))
        assertThat(pl, `is`(closeTo(Point.xyz(-1.0, 1.0, 0.0))))
        val pp = Point.xyz(1.0, -1.0, -3.0).projectTo(Plane(Point.xyz(1.0, -1.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        assertThat(pp, `is`(closeTo(Point.xyz(1.0, -1.0, 0.0))))
    }

    @Test
    fun testArea() {
        println("Area")
        val a = Point.xyz(1.0, 1.0, -1.0).area(Point.xyz(-3.0, 1.0, 2.0), Point.xyz(1.0, 4.0, -1.0))
        assertThat(a, `is`(closeTo(7.5)))
    }

    @Test
    fun testVolume() {
        println("Volume")
        val v = Point.xyz(0.0, 0.0, 0.0).volume(Point.xyz(1.0, 1.0, 0.0), Point.xyz(-1.0, 1.0, 0.0), Point.xyz(1.0, 1.0, -1.0))
        assertThat(v, `is`(closeTo(1.0 / 3.0)))
    }

    @Test
    fun testNormal() {
        println("Normal")
        val n = Point.xyz(1.0, 1.0, 0.0).normal(Point.xyz(-1.0, 1.0, 0.0), Point.xyz(0.0, 1.0, 1.0))
        assertThat(n.orThrow(), `is`(closeTo(Vector(0.0, 1.0, 0.0))))
    }

    @Test
    fun testAffineTransform() {
        println("AffineTransform")
        val t = Point.xyz(3.3, -2.4, -1.0).affineTransform(Translate(Vector(2.3, -5.4, -0.5)))
        assertThat(t, `is`(closeTo(Point.xyz(5.6, -7.8, -1.5))))
        val r = Point.xyz(1.0, 1.0, -1.0).affineTransform(Rotate(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0))
        assertThat(r, `is`(closeTo(Point.xyz(-1.0, 1.0, 1.0))))
        val s = Point.xyz(3.0, -2.0, -1.0).affineTransform(UniformlyScale(0.5))
        assertThat(s, `is`(closeTo(Point.xyz(1.5, -1.0, -0.5))))
    }
}

