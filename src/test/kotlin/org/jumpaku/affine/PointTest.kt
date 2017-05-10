package org.jumpaku.affine

import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.jumpaku.jsonAssertThat
import org.junit.Test




/**
 * Created by jumpaku on 2017/05/10.
 */
class PointTest {

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(Fuzzy(2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0).x).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0).y).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Fuzzy(2.0, 1.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0).y).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Fuzzy(2.0, 1.0,-2.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0,-2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0,-2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0,-2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Fuzzy(2.0, 1.0,-2.0, 3.0).r).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0,-2.0, 3.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0,-2.0, 3.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0,-2.0, 3.0).z).isEqualTo( 3.0, withPrecision(1.0e-10))

        assertThat(Fuzzy(2.0, Vector(1.0,-2.0, 3.0)).r).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, Vector(1.0,-2.0, 3.0)).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, Vector(1.0,-2.0, 3.0)).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, Vector(1.0,-2.0, 3.0)).z).isEqualTo( 3.0, withPrecision(1.0e-10))

        assertThat(Crisp(1.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0).y).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Crisp(1.0,-2.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0,-2.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0,-2.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0,-2.0).z).isEqualTo( 0.0, withPrecision(1.0e-10))

        assertThat(Crisp(1.0,-2.0, 3.0).r).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0,-2.0, 3.0).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0,-2.0, 3.0).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0,-2.0, 3.0).z).isEqualTo( 3.0, withPrecision(1.0e-10))

        assertThat(Crisp(Vector(1.0,-2.0, 3.0)).r).isEqualTo( 0.0, withPrecision(1.0e-10))
        assertThat(Crisp(Vector(1.0,-2.0, 3.0)).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Crisp(Vector(1.0,-2.0, 3.0)).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Crisp(Vector(1.0,-2.0, 3.0)).z).isEqualTo( 3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testMembership() {
        println("Membership")
        val p0 = Crisp(1.0, -2.0)
        val p1 = Crisp(2.0, -2.0)
        val p2 = Crisp(3.0, -2.0)
        val p3 = Crisp(4.0, -2.0)

        assertThat(Fuzzy(2.0, 1.0, -2.0).membership(p0).value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0, -2.0).membership(p1).value).isEqualTo(0.5, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0, -2.0).membership(p2).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(Fuzzy(2.0, 1.0, -2.0).membership(p3).value).isEqualTo(0.0, withPrecision(1.0e-10))

        assertThat(Crisp(1.0, -2.0).membership(p0).value).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0, -2.0).membership(p1).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0, -2.0).membership(p2).value).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(Crisp(1.0, -2.0).membership(p3).value).isEqualTo(0.0, withPrecision(1.0e-10))
    }

    @Test
    fun testPossibility() {
        println("Possibility")
        val p0  = Fuzzy(1.0, 0.0, 1.0)
        val p1  = Fuzzy(2.0, 0.0, 1.0)
        val p2  = Fuzzy(0.5, 1.0, 1.0)
        val p3  = Fuzzy(2.0, 1.0, 1.0)
        val p4  = Fuzzy(1.0, 2.0, 1.0)
        val p5  = Fuzzy(4.0, 2.0, 1.0)
        val p6  = Fuzzy(1.0, 3.0, 1.0)
        val p7  = Fuzzy(8.0, 3.0, 1.0)
        val p8  = Crisp(     0.0, 1.0)
        val p9  = Crisp(     1.0, 1.0)
        val p10 = Crisp(     2.0, 1.0)
        val p11 = Crisp(     3.0, 1.0)

        val f = Fuzzy(2.0, 0.0, 1.0)
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

        val c = Crisp(0.0, 1.0)
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
        val p0  = Fuzzy(1.0, 0.0, 1.0)
        val p1  = Fuzzy(2.0, 0.0, 1.0)
        val p2  = Fuzzy(0.5, 1.0, 1.0)
        val p3  = Fuzzy(2.0, 1.0, 1.0)
        val p4  = Fuzzy(1.0, 2.0, 1.0)
        val p5  = Fuzzy(4.0, 2.0, 1.0)
        val p6  = Fuzzy(1.0, 3.0, 1.0)
        val p7  = Fuzzy(8.0, 3.0, 1.0)
        val p8  = Crisp(     0.0, 1.0)
        val p9  = Crisp(     1.0, 1.0)
        val p10 = Crisp(     2.0, 1.0)
        val p11 = Crisp(     3.0, 1.0)

        val f = Fuzzy(2.0, 0.0, 1.0)
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

        val c = Crisp(0.0, 1.0)
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
        val c0 = Crisp(      1.0)
        val c1 = Crisp(      2.0)
        val f0 = Fuzzy(10.0, 1.0)
        val f1 = Fuzzy(10.0, 2.0)
        val f2 = Fuzzy(20.0, 2.0)

        pointAssertThat(c0.divide( 0.3, c1)).isEqualToPoint(Fuzzy(0.0, 1.3, 0.0, 0.0))
        pointAssertThat(c0.divide(-1.0, c1)).isEqualToPoint(Fuzzy(0.0, 0.0, 0.0, 0.0))
        pointAssertThat(c0.divide( 2.0, c1)).isEqualToPoint(Fuzzy(0.0, 3.0, 0.0, 0.0))
        pointAssertThat(c0.divide( 0.0, c1)).isEqualToPoint(Fuzzy(0.0, 1.0, 0.0, 0.0))
        pointAssertThat(c0.divide( 1.0, c1)).isEqualToPoint(Fuzzy(0.0, 2.0, 0.0, 0.0))

        pointAssertThat(f0.divide( 0.3, c1)).isEqualToPoint(Fuzzy( 7.0, 1.3, 0.0, 0.0))
        pointAssertThat(f0.divide(-1.0, c1)).isEqualToPoint(Fuzzy(20.0, 0.0, 0.0, 0.0))
        pointAssertThat(f0.divide( 2.0, c1)).isEqualToPoint(Fuzzy(10.0, 3.0, 0.0, 0.0))
        pointAssertThat(f0.divide( 0.0, c1)).isEqualToPoint(Fuzzy(10.0, 1.0, 0.0, 0.0))
        pointAssertThat(f0.divide( 1.0, c1)).isEqualToPoint(Fuzzy( 0.0, 2.0, 0.0, 0.0))

        pointAssertThat(c0.divide( 0.3, f1)).isEqualToPoint(Fuzzy( 3.0, 1.3, 0.0, 0.0))
        pointAssertThat(c0.divide(-1.0, f1)).isEqualToPoint(Fuzzy(10.0, 0.0, 0.0, 0.0))
        pointAssertThat(c0.divide( 2.0, f1)).isEqualToPoint(Fuzzy(20.0, 3.0, 0.0, 0.0))
        pointAssertThat(c0.divide( 0.0, f1)).isEqualToPoint(Fuzzy( 0.0, 1.0, 0.0, 0.0))
        pointAssertThat(c0.divide( 1.0, f1)).isEqualToPoint(Fuzzy(10.0, 2.0, 0.0, 0.0))

        pointAssertThat(f0.divide( 0.3, f2)).isEqualToPoint(Fuzzy(13.0, 1.3, 0.0, 0.0))
        pointAssertThat(f0.divide(-1.0, f2)).isEqualToPoint(Fuzzy(40.0, 0.0, 0.0, 0.0))
        pointAssertThat(f0.divide( 2.0, f2)).isEqualToPoint(Fuzzy(50.0, 3.0, 0.0, 0.0))
        pointAssertThat(f0.divide( 0.0, f2)).isEqualToPoint(Fuzzy(10.0, 1.0, 0.0, 0.0))
        pointAssertThat(f0.divide( 1.0, f2)).isEqualToPoint(Fuzzy(20.0, 2.0, 0.0, 0.0))
    }

    @Test
    fun testToVector() {
        println("ToVector")
        val f = Fuzzy(2.0, 1.0,-2.0, 3.0).toVector()
        vectorAssertThat(f).isEqualToVector(Vector(1.0,-2.0, 3.0))
        val c = Crisp(1.0,-2.0, 3.0).toVector()
        vectorAssertThat(c).isEqualToVector(Vector(1.0,-2.0, 3.0))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val f = Fuzzy(2.0, 1.0,-2.0, 3.0).toCrisp()
        pointAssertThat(f).isEqualToPoint(Crisp(1.0,-2.0, 3.0))
        val c = Crisp(1.0,-2.0, 3.0).toCrisp()
        pointAssertThat(c).isEqualToPoint(Crisp(1.0,-2.0, 3.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val f = Fuzzy(2.0, 1.0,-2.0, 3.0)
        val c = Crisp(1.0,-2.0, 3.0)
        pointAssertThat(Point.fromJson("""{"r":2.0, "x":1.0, "y":-2.0, "z":3.0}""")!!).isEqualToPoint(f)
        jsonAssertThat(Point.toJson(f)).isEqualToWithoutWhitespace("""{"r":2.0, "x":1.0, "y":-2.0, "z":3.0}""")
        jsonAssertThat(Point.toJson(c)).isEqualToWithoutWhitespace("""{"r":0.0, "x":1.0, "y":-2.0, "z":3.0}""")
        jsonAssertThat(f.toString()).isEqualToWithoutWhitespace("""{"r":2.0, "x":1.0, "y":-2.0, "z":3.0}""")
        jsonAssertThat(c.toString()).isEqualToWithoutWhitespace("""{"r":0.0, "x":1.0, "y":-2.0, "z":3.0}""")

        assertThat(Point.fromJson("""{"x":null, "y"-2.0, "z":3.0}""")).isNull()
        assertThat(Point.fromJson("""{"x":1.0"y"-2.0, "z":3.0}""")).isNull()
        assertThat(Point.fromJson(""""x":1.0, "y":-2.0, "z":3.0}""")).isNull()
    }

    @Test
    fun testPlus() {
        println("Plus")
        val p = Crisp(-1.0, -2.0, 4.0).plus(Vector(1.0,-2.0, 3.0))
        pointAssertThat(p).isEqualToPoint(Crisp(0.0, -4.0, 7.0))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val p = Crisp(-1.0, -2.0, 4.0).minus(Crisp(1.0,-2.0, 3.0))
        vectorAssertThat(p).isEqualToVector(Vector(-2.0, 0.0, 1.0))
    }

    @Test
    fun testDist() {
        println("Dist")
        val d = Crisp(-1.0, -2.0, 4.0).dist(Crisp(1.0, -2.0, 3.0))
        assertThat(d).isEqualTo(FastMath.sqrt(5.0), withPrecision(1.0e-10))
    }

    @Test
    fun testDistSquare() {
        println("DistSquare")
        val d = Crisp(-1.0, -2.0, 4.0).distSquare(Crisp(1.0, -2.0, 3.0))
        assertThat(d).isEqualTo(5.0, withPrecision(1.0e-10))
    }

    @Test
    fun testArea() {
        println("Area")
        val a = Crisp(1.0, 1.0, -1.0).area(Crisp(-3.0, 1.0, 2.0), Crisp(1.0, 4.0, -1.0))
        assertThat(a).isEqualTo(7.5, withPrecision(1.0e-10))
    }

    @Test
    fun testVolume() {
        println("Volume")
        val v = Crisp(0.0, 0.0, 0.0).volume(Crisp(1.0, 1.0, 0.0), Crisp(-1.0, 1.0, 0.0), Crisp(1.0, 1.0, -1.0))
        assertThat(v).isEqualTo(1.0/3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testNormal() {
        println("Normal")
        val n = Crisp(1.0, 1.0, 0.0).normal(Crisp(-1.0, 1.0, 0.0), Crisp(0.0, 1.0, 1.0))
        vectorAssertThat(n).isEqualToVector(Vector(0.0, 1.0, 0.0))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val t = Crisp(3.3, -2.4, -1.0).transform(translation(Vector(2.3, -5.4, -0.5)))
        pointAssertThat(t).isEqualToPoint(Crisp(5.6, -7.8, -1.5))
        val r = Crisp(1.0, 1.0, -1.0).transform(rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0))
        pointAssertThat(r).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val s = Crisp(3.0, -2.0, -1.0).transform(scaling(0.5, 0.5, 2.0))
        pointAssertThat(s).isEqualToPoint(Crisp(1.5, -1.0, -2.0))
    }
}