package jumpaku.core.affine

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.json.parseToJson
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import jumpaku.core.json.prettyGson
import org.junit.Test


class PointTest {

    @Test
    fun testProperties() {
        println("Properties")

        assertThat(Point.xr(1.0, 2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xr(1.0, 2.0).y).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xr(1.0, 2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xr(1.0, 2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))

        assertThat(Point.xyr(1.0, -2.0, 2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xyr(1.0, -2.0, 2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xyr(1.0, -2.0, 2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xyr(1.0, -2.0, 2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))

        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).z).isEqualTo( 3.0, withPrecision(1.0e-10))
        assertThat(Point.xyzr(1.0, -2.0, 3.0, 2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))

        assertThat(Point.x(1.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.x(1.0).y).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.x(1.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.x(1.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Point.xy(1.0, -2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xy(1.0, -2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xy(1.0, -2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xy(1.0, -2.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Point.xyz(1.0, -2.0, 3.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xyz(1.0, -2.0, 3.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xyz(1.0, -2.0, 3.0).z).isEqualTo( 3.0, withPrecision(1.0e-10))
        assertThat(Point.xyz(1.0, -2.0, 3.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))
    }

    @Test
    fun testMembership() {
        println("Membership")
        val p0 = Point.xy(1.0, -2.0)
        val p1 = Point.xy(2.0, -2.0)
        val p2 = Point.xy(3.0, -2.0)
        val p3 = Point.xy(4.0, -2.0)

        val f = Point.xyr(1.0, -2.0, 2.0)
        assertThat(f.membership(p0).value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(f.membership(p1).value).isEqualTo(0.5, withPrecision(1.0e-10))
        assertThat(f.membership(p2).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(f.membership(p3).value).isEqualTo(0.0, withPrecision(1.0e-10))

        val c = Point.xy(1.0, -2.0)
        assertThat(c.membership(p0).value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(c.membership(p1).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(c.membership(p2).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(c.membership(p3).value).isEqualTo(0.0, withPrecision(1.0e-10))
    }

    @Test
    fun testPossibility() {
        println("Possibility")
        val p0  = Point.xyr(0.0, 1.0, 1.0)
        val p1  = Point.xyr(0.0, 1.0, 2.0)
        val p2  = Point.xyr(1.0, 1.0, 0.5)
        val p3  = Point.xyr(1.0, 1.0, 2.0)
        val p4  = Point.xyr(2.0, 1.0, 1.0)
        val p5  = Point.xyr(2.0, 1.0, 4.0)
        val p6  = Point.xyr(3.0, 1.0, 1.0)
        val p7  = Point.xyr(3.0, 1.0, 8.0)
        val p8  = Point.xy(0.0, 1.0)
        val p9  = Point.xy(1.0, 1.0)
        val p10 = Point.xy(2.0, 1.0)
        val p11 = Point.xy(3.0, 1.0)

        val f = Point.xyr(0.0, 1.0, 2.0)
        assertThat(f.isPossible(p0 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(f.isPossible(p1 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(f.isPossible(p2 ).value).isEqualTo(3.0 / 5, withPrecision(1.0e-10))
        assertThat(f.isPossible(p3 ).value).isEqualTo(0.75,    withPrecision(1.0e-10))
        assertThat(f.isPossible(p4 ).value).isEqualTo(1.0 / 3, withPrecision(1.0e-10))
        assertThat(f.isPossible(p5 ).value).isEqualTo(2.0 / 3, withPrecision(1.0e-10))
        assertThat(f.isPossible(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isPossible(p7 ).value).isEqualTo(0.7,     withPrecision(1.0e-10))
        assertThat(f.isPossible(p8 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(f.isPossible(p9 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(f.isPossible(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isPossible(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))

        val c = Point.xy(0.0, 1.0)
        assertThat(c.isPossible(p0 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p1 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p2 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p3 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p4 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p5 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p7 ).value).isEqualTo(5.0 / 8, withPrecision(1.0e-10))
        assertThat(c.isPossible(p8 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p9 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isPossible(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))
    }

    @Test
    fun testNecessity() {
        println("Necessity")
        val p0  = Point.xyr(0.0, 1.0, 1.0)
        val p1  = Point.xyr(0.0, 1.0, 2.0)
        val p2  = Point.xyr(1.0, 1.0, 0.5)
        val p3  = Point.xyr(1.0, 1.0, 2.0)
        val p4  = Point.xyr(2.0, 1.0, 1.0)
        val p5  = Point.xyr(2.0, 1.0, 4.0)
        val p6  = Point.xyr(3.0, 1.0, 1.0)
        val p7  = Point.xyr(3.0, 1.0, 8.0)
        val p8  = Point.xy(0.0, 1.0)
        val p9  = Point.xy(1.0, 1.0)
        val p10 = Point.xy(2.0, 1.0)
        val p11 = Point.xy(3.0, 1.0)

        val f = Point.xyr(0.0, 1.0, 2.0)
        assertThat(f.isNecessary(p0 ).value).isEqualTo(1.0 / 3, withPrecision(1.0e-10))
        assertThat(f.isNecessary(p1 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p2 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p3 ).value).isEqualTo(0.25,    withPrecision(1.0e-10))
        assertThat(f.isNecessary(p4 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p5 ).value).isEqualTo(1.0 / 3, withPrecision(1.0e-10))
        assertThat(f.isNecessary(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p7 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p8 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p9 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.isNecessary(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))

        val c = Point.xy(0.0, 1.0)
        assertThat(c.isNecessary(p0 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p1 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p2 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p3 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p4 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p5 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p7 ).value).isEqualTo(5.0 / 8, withPrecision(1.0e-10))
        assertThat(c.isNecessary(p8 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p9 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.isNecessary(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))
    }

    @Test
    fun testDivide() {
        println("Divide")
        val c0 = Point.x(1.0)
        val c1 = Point.x(2.0)
        val f0 = Point.xr(1.0, 10.0)
        val f1 = Point.xr(2.0, 10.0)
        val f2 = Point.xr(2.0, 20.0)

        pointAssertThat(c0.divide(0.3, c1)).isEqualToPoint(Point.xr(1.3, 0.0))
        pointAssertThat(c0.divide(-1.0, c1)).isEqualToPoint(Point.xr(0.0, 0.0))
        pointAssertThat(c0.divide(2.0, c1)).isEqualToPoint(Point.xr(3.0, 0.0))
        pointAssertThat(c0.divide(0.0, c1)).isEqualToPoint(Point.xr(1.0, 0.0))
        pointAssertThat(c0.divide(1.0, c1)).isEqualToPoint(Point.xr(2.0, 0.0))

        pointAssertThat(f0.divide(0.3, c1)).isEqualToPoint(Point.xr(1.3, 7.0))
        pointAssertThat(f0.divide(-1.0, c1)).isEqualToPoint(Point.xr(0.0, 20.0))
        pointAssertThat(f0.divide(2.0, c1)).isEqualToPoint(Point.xr(3.0, 10.0))
        pointAssertThat(f0.divide(0.0, c1)).isEqualToPoint(Point.xr(1.0, 10.0))
        pointAssertThat(f0.divide(1.0, c1)).isEqualToPoint(Point.xr(2.0, 0.0))

        pointAssertThat(c0.divide(0.3, f1)).isEqualToPoint(Point.xr(1.3, 3.0))
        pointAssertThat(c0.divide(-1.0, f1)).isEqualToPoint(Point.xr(0.0, 10.0))
        pointAssertThat(c0.divide(2.0, f1)).isEqualToPoint(Point.xr(3.0, 20.0))
        pointAssertThat(c0.divide(0.0, f1)).isEqualToPoint(Point.xr(1.0, 0.0))
        pointAssertThat(c0.divide(1.0, f1)).isEqualToPoint(Point.xr(2.0, 10.0))

        pointAssertThat(f0.divide(0.3, f2)).isEqualToPoint(Point.xr(1.3, 13.0))
        pointAssertThat(f0.divide(-1.0, f2)).isEqualToPoint(Point.xr(0.0, 40.0))
        pointAssertThat(f0.divide(2.0, f2)).isEqualToPoint(Point.xr(3.0, 50.0))
        pointAssertThat(f0.divide(0.0, f2)).isEqualToPoint(Point.xr(1.0, 10.0))
        pointAssertThat(f0.divide(1.0, f2)).isEqualToPoint(Point.xr(2.0, 20.0))
    }

    @Test
    fun testToVector() {
        println("ToVector")
        val f = Point.xyzr(1.0, -2.0, 3.0, 2.0).toVector()
        vectorAssertThat(f).isEqualToVector(Vector(1.0, -2.0, 3.0))
        val c = Point.xyz(1.0, -2.0, 3.0).toVector()
        vectorAssertThat(c).isEqualToVector(Vector(1.0, -2.0, 3.0))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val f = Point.xyzr(1.0, -2.0, 3.0, 2.0).toCrisp()
        pointAssertThat(f).isEqualToPoint(Point.xyz(1.0, -2.0, 3.0))

        val c = Point.xyz(1.0, -2.0, 3.0).toCrisp()
        pointAssertThat(c).isEqualToPoint(Point.xyz(1.0, -2.0, 3.0))
    }

    @Test
    fun testToArray() {
        println("ToArray")
        val a = Point(1.0, -2.0, 3.0, 2.0).toArray()
        assertThat(a[0]).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(a[1]).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(a[2]).isEqualTo( 3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val f = Point.xyzr(1.0, -2.0, 3.0, 2.0)
        val c = Point.xyz(1.0, -2.0, 3.0)
        pointAssertThat(f.toString().parseToJson().get().point).isEqualToPoint(f)
        pointAssertThat(c.toString().parseToJson().get().point).isEqualToPoint(c)
    }

    @Test
    fun testPlus() {
        println("Plus")
        val p0 = Point.xyz(-1.0, -2.0, 4.0).plus(Vector(1.0, -2.0, 3.0))
        pointAssertThat(p0).isEqualToPoint(Point.xyz(0.0, -4.0, 7.0))
        val p1 = Point.xyz(-1.0, -2.0, 4.0).minus(Vector(1.0, -2.0, 3.0))
        pointAssertThat(p1).isEqualToPoint(Point.xyz(-2.0, 0.0, 1.0))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val p = Point.xyz(-1.0, -2.0, 4.0).minus(Point.xyz(1.0, -2.0, 3.0))
        vectorAssertThat(p).isEqualToVector(Vector(-2.0, 0.0, 1.0))
    }

    @Test
    fun testDist() {
        println("Dist")
        val dp = Point.xyz(-1.0, -2.0, 4.0).dist(Point.xyz(1.0, -2.0, 3.0))
        assertThat(dp).isEqualTo(FastMath.sqrt(5.0), withPrecision(1.0e-10))
        val dl = Point.xyz(1.0, -1.0, 0.0).dist(line(Point.xyz(-3.0, -1.0, 0.0), Vector(4.0, 3.0, 0.0)).get())
        assertThat(dl).isEqualTo(12/5.0, withPrecision(1.0e-10))
        val dplane = Point.xyz(1.0, -1.0, -3.0).dist(plane(Point.xyz(1.0, -1.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)).get())
        assertThat(dplane).isEqualTo(3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testDistSquare() {
        println("DistSquare")
        val dp = Point.xyz(-1.0, -2.0, 4.0).distSquare(Point.xyz(1.0, -2.0, 3.0))
        assertThat(dp).isEqualTo(5.0, withPrecision(1.0e-10))
        val dl = Point.xyz(1.0, -1.0, 0.0).distSquare(line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)).get())
        assertThat(dl).isEqualTo(144/25.0, withPrecision(1.0e-10))
        val dplane = Point.xyz(1.0, -1.0, -3.0).distSquare(plane(Point.xyz(2.0, -2.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)).get())
        assertThat(dplane).isEqualTo(9.0, withPrecision(1.0e-10))
    }

    @Test
    fun testProjectTo() {
        println("ProjectTo")
        val pl = Point.xyz(1.0, -1.0, 0.0).projectTo(line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 3.0, 0.0)).get())
        pointAssertThat(pl).isEqualToPoint(Point.xyz(-1.0, 1.0, 0.0))
        val pp = Point.xyz(1.0, -1.0, -3.0).projectTo(plane(Point.xyz(1.0, -1.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)).get())
        pointAssertThat(pp).isEqualToPoint(Point.xyz(1.0, -1.0, 0.0))
    }

    @Test
    fun testArea() {
        println("Area")
        val a = Point.xyz(1.0, 1.0, -1.0).area(Point.xyz(-3.0, 1.0, 2.0), Point.xyz(1.0, 4.0, -1.0))
        assertThat(a).isEqualTo(7.5, withPrecision(1.0e-10))
    }

    @Test
    fun testVolume() {
        println("Volume")
        val v = Point.xyz(0.0, 0.0, 0.0).volume(Point.xyz(1.0, 1.0, 0.0), Point.xyz(-1.0, 1.0, 0.0), Point.xyz(1.0, 1.0, -1.0))
        assertThat(v).isEqualTo(1.0/3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testNormal() {
        println("Normal")
        val n = Point.xyz(1.0, 1.0, 0.0).normal(Point.xyz(-1.0, 1.0, 0.0), Point.xyz(0.0, 1.0, 1.0))
        vectorAssertThat(n).isEqualToVector(Vector(0.0, 1.0, 0.0))
    }

    @Test
    fun testTransform() {
        println("Affine")
        val t = Point.xyz(3.3, -2.4, -1.0).transform(translation(Vector(2.3, -5.4, -0.5)))
        pointAssertThat(t).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = Point.xyz(1.0, 1.0, -1.0).transform(rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = Point.xyz(3.0, -2.0, -1.0).transform(scaling(0.5, 0.5, 2.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }
}