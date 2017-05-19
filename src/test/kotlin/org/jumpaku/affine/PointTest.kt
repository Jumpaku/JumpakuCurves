package org.jumpaku.affine

import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.jumpaku.jsonAssertThat
import org.junit.Test


class PointTest {

    @Test
    fun testProperties() {
        println("Properties")

        assertThat(Point.xr(1.0, 2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xr(1.0, 2.0).y).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xr(1.0, 2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xr(1.0, 2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))

        assertThat(Point.xyr(1.0,-2.0, 2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xyr(1.0,-2.0, 2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xyr(1.0,-2.0, 2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xyr(1.0,-2.0, 2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))

        assertThat(Point.xyzr(1.0,-2.0, 3.0, 2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xyzr(1.0,-2.0, 3.0, 2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xyzr(1.0,-2.0, 3.0, 2.0).z).isEqualTo( 3.0, withPrecision(1.0e-10))
        assertThat(Point.xyzr(1.0,-2.0, 3.0, 2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))

        assertThat(Point.x(1.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.x(1.0).y).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.x(1.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.x(1.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Point.xy(1.0,-2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xy(1.0,-2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xy(1.0,-2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Point.xy(1.0,-2.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Point.xyz(1.0,-2.0, 3.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Point.xyz(1.0,-2.0, 3.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Point.xyz(1.0,-2.0, 3.0).z).isEqualTo( 3.0, withPrecision(1.0e-10))
        assertThat(Point.xyz(1.0,-2.0, 3.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))
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
        val p8  = Point.xy( 0.0, 1.0     )
        val p9  = Point.xy( 1.0, 1.0     )
        val p10 = Point.xy( 2.0, 1.0     )
        val p11 = Point.xy( 3.0, 1.0     )

        val f = Point.xyr(0.0, 1.0, 2.0)
        assertThat(f.possibility(p0 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(f.possibility(p1 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(f.possibility(p2 ).value).isEqualTo(3.0 / 5, withPrecision(1.0e-10))
        assertThat(f.possibility(p3 ).value).isEqualTo(0.75,    withPrecision(1.0e-10))
        assertThat(f.possibility(p4 ).value).isEqualTo(1.0 / 3, withPrecision(1.0e-10))
        assertThat(f.possibility(p5 ).value).isEqualTo(2.0 / 3, withPrecision(1.0e-10))
        assertThat(f.possibility(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.possibility(p7 ).value).isEqualTo(0.7,     withPrecision(1.0e-10))
        assertThat(f.possibility(p8 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(f.possibility(p9 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(f.possibility(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.possibility(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))

        val c = Point.xy(0.0, 1.0)
        assertThat(c.possibility(p0 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p1 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p2 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p3 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.possibility(p4 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p5 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.possibility(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p7 ).value).isEqualTo(5.0 / 8, withPrecision(1.0e-10))
        assertThat(c.possibility(p8 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p9 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.possibility(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))
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
        val p8  = Point.xy( 0.0, 1.0)
        val p9  = Point.xy( 1.0, 1.0)
        val p10 = Point.xy( 2.0, 1.0)
        val p11 = Point.xy( 3.0, 1.0)

        val f = Point.xyr(0.0, 1.0, 2.0)
        assertThat(f.necessity(p0 ).value).isEqualTo(1.0 / 3, withPrecision(1.0e-10))
        assertThat(f.necessity(p1 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(f.necessity(p2 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.necessity(p3 ).value).isEqualTo(0.25,    withPrecision(1.0e-10))
        assertThat(f.necessity(p4 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.necessity(p5 ).value).isEqualTo(1.0 / 3, withPrecision(1.0e-10))
        assertThat(f.necessity(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.necessity(p7 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(f.necessity(p8 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.necessity(p9 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.necessity(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(f.necessity(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))

        val c = Point.xy(0.0, 1.0)
        assertThat(c.necessity(p0 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p1 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p2 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p3 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.necessity(p4 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p5 ).value).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(c.necessity(p6 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p7 ).value).isEqualTo(5.0 / 8, withPrecision(1.0e-10))
        assertThat(c.necessity(p8 ).value).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p9 ).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p10).value).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(c.necessity(p11).value).isEqualTo(0.0,     withPrecision(1.0e-10))
    }

    @Test
    fun testDivide() {
        println("Divide")
        val c0 = Point.x( 1.0)
        val c1 = Point.x( 2.0)
        val f0 = Point.xr(1.0, 10.0)
        val f1 = Point.xr(2.0, 10.0)
        val f2 = Point.xr(2.0, 20.0)

        pointAssertThat(c0.divide( 0.3, c1)).isEqualToPoint(Point.xr(1.3,  0.0))
        pointAssertThat(c0.divide(-1.0, c1)).isEqualToPoint(Point.xr(0.0,  0.0))
        pointAssertThat(c0.divide( 2.0, c1)).isEqualToPoint(Point.xr(3.0,  0.0))
        pointAssertThat(c0.divide( 0.0, c1)).isEqualToPoint(Point.xr(1.0,  0.0))
        pointAssertThat(c0.divide( 1.0, c1)).isEqualToPoint(Point.xr(2.0,  0.0))

        pointAssertThat(f0.divide( 0.3, c1)).isEqualToPoint(Point.xr(1.3,  7.0))
        pointAssertThat(f0.divide(-1.0, c1)).isEqualToPoint(Point.xr(0.0, 20.0))
        pointAssertThat(f0.divide( 2.0, c1)).isEqualToPoint(Point.xr(3.0, 10.0))
        pointAssertThat(f0.divide( 0.0, c1)).isEqualToPoint(Point.xr(1.0, 10.0))
        pointAssertThat(f0.divide( 1.0, c1)).isEqualToPoint(Point.xr(2.0,  0.0))

        pointAssertThat(c0.divide( 0.3, f1)).isEqualToPoint(Point.xr(1.3,  3.0))
        pointAssertThat(c0.divide(-1.0, f1)).isEqualToPoint(Point.xr(0.0, 10.0))
        pointAssertThat(c0.divide( 2.0, f1)).isEqualToPoint(Point.xr(3.0, 20.0))
        pointAssertThat(c0.divide( 0.0, f1)).isEqualToPoint(Point.xr(1.0,  0.0))
        pointAssertThat(c0.divide( 1.0, f1)).isEqualToPoint(Point.xr(2.0, 10.0))

        pointAssertThat(f0.divide( 0.3, f2)).isEqualToPoint(Point.xr(1.3, 13.0))
        pointAssertThat(f0.divide(-1.0, f2)).isEqualToPoint(Point.xr(0.0, 40.0))
        pointAssertThat(f0.divide( 2.0, f2)).isEqualToPoint(Point.xr(3.0, 50.0))
        pointAssertThat(f0.divide( 0.0, f2)).isEqualToPoint(Point.xr(1.0, 10.0))
        pointAssertThat(f0.divide( 1.0, f2)).isEqualToPoint(Point.xr(2.0, 20.0))
    }

    @Test
    fun testToVector() {
        println("ToVector")
        val f = Point.xyzr(1.0,-2.0, 3.0, 2.0).toVector()
        vectorAssertThat(f).isEqualToVector(Vector(1.0,-2.0, 3.0))
        val c = Point.xyz(1.0,-2.0, 3.0).toVector()
        vectorAssertThat(c).isEqualToVector(Vector(1.0,-2.0, 3.0))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val f = Point.xyzr(1.0,-2.0, 3.0, 2.0).toCrisp()
        pointAssertThat(f).isEqualToPoint(Point.xyz(1.0,-2.0, 3.0))

        val c = Point.xyz(1.0,-2.0, 3.0).toCrisp()
        pointAssertThat(c).isEqualToPoint(Point.xyz(1.0,-2.0, 3.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val f = Point.xyzr(1.0,-2.0, 3.0, 2.0)
        val c = Point.xyz(1.0,-2.0, 3.0)
        pointAssertThat(PointJson.fromJson(PointJson.toJson(f)).get()).isEqualToPoint(f)
        pointAssertThat(PointJson.fromJson(PointJson.toJson(c)).get()).isEqualToPoint(c)
        pointAssertThat(PointJson.fromJson(f.toString()).get()).isEqualToPoint(f)
        pointAssertThat(PointJson.fromJson(c.toString()).get()).isEqualToPoint(c)

        assertThat(PointJson.fromJson("""{"x":null, "y"-2.0, "z":3.0, "r":2.0}""").isEmpty).isTrue()
        assertThat(PointJson.fromJson("""{"x":1.0"y"-2.0, "z":3.0, "r":2.0}""").isEmpty).isTrue()
        assertThat(PointJson.fromJson(""""x":1.0, "y":-2.0, "z":3.0, "r":2.0}""").isEmpty).isTrue()
    }

    @Test
    fun testPlus() {
        println("Plus")
        val p = Point.xyz(-1.0, -2.0, 4.0).plus(Vector(1.0,-2.0, 3.0))
        pointAssertThat(p).isEqualToPoint(Point.xyz(0.0, -4.0, 7.0))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val p = Point.xyz(-1.0, -2.0, 4.0).minus(Point.xyz(1.0,-2.0, 3.0))
        vectorAssertThat(p).isEqualToVector(Vector(-2.0, 0.0, 1.0))
    }

    @Test
    fun testDist() {
        println("Dist")
        val d = Point.xyz(-1.0, -2.0, 4.0).dist(Point.xyz(1.0, -2.0, 3.0))
        assertThat(d).isEqualTo(FastMath.sqrt(5.0), withPrecision(1.0e-10))
    }

    @Test
    fun testDistSquare() {
        println("DistSquare")
        val d = Point.xyz(-1.0, -2.0, 4.0).distSquare(Point.xyz(1.0, -2.0, 3.0))
        assertThat(d).isEqualTo(5.0, withPrecision(1.0e-10))
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
        println("Transform")
        val t = Point.xyz(3.3, -2.4, -1.0).transform(Transform.translation(Vector(2.3, -5.4, -0.5)))
        pointAssertThat(t).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = Point.xyz(1.0, 1.0, -1.0).transform(Transform.rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = Point.xyz(3.0, -2.0, -1.0).transform(Transform.scaling(0.5, 0.5, 2.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }
}