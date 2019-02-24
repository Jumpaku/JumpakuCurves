package jumpaku.core.test.geom

import jumpaku.core.geom.*
import jumpaku.core.transform.Rotate
import jumpaku.core.transform.Translate
import jumpaku.core.transform.UniformlyScale
import jumpaku.core.json.parseJson
import jumpaku.core.test.shouldBeCloseTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class PointTest {

    val f = Point.xyzr(1.0, -2.0, 3.0, 2.0)
    val c = Point.xyz(1.0, -2.0, 3.0)

    @Test
    fun testCreate() {
        println("Create")
        Point.xr(1.0, 2.0).x.shouldBeCloseTo(1.0)
        Point.xr(1.0, 2.0).y.shouldBeCloseTo(0.0)
        Point.xr(1.0, 2.0).z.shouldBeCloseTo(0.0)
        Point.xr(1.0, 2.0).r.shouldBeCloseTo(2.0)

        Point.xyr(1.0, -2.0, 2.0).x.shouldBeCloseTo(1.0)
        Point.xyr(1.0, -2.0, 2.0).y.shouldBeCloseTo(-2.0)
        Point.xyr(1.0, -2.0, 2.0).z.shouldBeCloseTo(0.0)
        Point.xyr(1.0, -2.0, 2.0).r.shouldBeCloseTo(2.0)

        Point.xyzr(1.0, -2.0, 3.0, 2.0).x.shouldBeCloseTo(1.0)
        Point.xyzr(1.0, -2.0, 3.0, 2.0).y.shouldBeCloseTo(-2.0)
        Point.xyzr(1.0, -2.0, 3.0, 2.0).z.shouldBeCloseTo(3.0)
        Point.xyzr(1.0, -2.0, 3.0, 2.0).r.shouldBeCloseTo(2.0)

        Point.x(1.0).x.shouldBeCloseTo(1.0)
        Point.x(1.0).y.shouldBeCloseTo(0.0)
        Point.x(1.0).z.shouldBeCloseTo(0.0)
        Point.x(1.0).r.shouldBeCloseTo(0.0)

        Point.xy(1.0, -2.0).x.shouldBeCloseTo(1.0)
        Point.xy(1.0, -2.0).y.shouldBeCloseTo(-2.0)
        Point.xy(1.0, -2.0).z.shouldBeCloseTo(0.0)
        Point.xy(1.0, -2.0).r.shouldBeCloseTo(0.0)

        Point.xyz(1.0, -2.0, 3.0).x.shouldBeCloseTo(1.0)
        Point.xyz(1.0, -2.0, 3.0).y.shouldBeCloseTo(-2.0)
        Point.xyz(1.0, -2.0, 3.0).z.shouldBeCloseTo(3.0)
        Point.xyz(1.0, -2.0, 3.0).r.shouldBeCloseTo(0.0)
    }

    @Test
    fun testMembership() {
        println("Membership")
        val p0 = Point.xy(1.0, -2.0)
        val p1 = Point.xy(2.0, -2.0)
        val p2 = Point.xy(3.0, -2.0)
        val p3 = Point.xy(4.0, -2.0)

        val f = Point.xyr(1.0, -2.0, 2.0)
        f.membership(p0).value.shouldBeCloseTo(1.0)
        f.membership(p1).value.shouldBeCloseTo(0.5)
        f.membership(p2).value.shouldBeCloseTo(0.0)
        f.membership(p3).value.shouldBeCloseTo(0.0)

        val c = Point.xy(1.0, -2.0)
        c.membership(p0).value.shouldBeCloseTo(1.0)
        c.membership(p1).value.shouldBeCloseTo(0.0)
        c.membership(p2).value.shouldBeCloseTo(0.0)
        c.membership(p3).value.shouldBeCloseTo(0.0)
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
        f.isPossible(p0 ).value.shouldBeCloseTo(1.0)
        f.isPossible(p1 ).value.shouldBeCloseTo(1.0)
        f.isPossible(p2 ).value.shouldBeCloseTo(3.0 / 5)
        f.isPossible(p3 ).value.shouldBeCloseTo(0.75)
        f.isPossible(p4 ).value.shouldBeCloseTo(1.0 / 3)
        f.isPossible(p5 ).value.shouldBeCloseTo(2.0 / 3)
        f.isPossible(p6 ).value.shouldBeCloseTo(0.0)
        f.isPossible(p7 ).value.shouldBeCloseTo(0.7)
        f.isPossible(p8 ).value.shouldBeCloseTo(1.0)
        f.isPossible(p9 ).value.shouldBeCloseTo(0.5)
        f.isPossible(p10).value.shouldBeCloseTo(0.0)
        f.isPossible(p11).value.shouldBeCloseTo(0.0)

        val c = Point.xy(0.0, 1.0)
        c.isPossible(p0 ).value.shouldBeCloseTo(1.0)
        c.isPossible(p1 ).value.shouldBeCloseTo(1.0)
        c.isPossible(p2 ).value.shouldBeCloseTo(0.0)
        c.isPossible(p3 ).value.shouldBeCloseTo(0.5)
        c.isPossible(p4 ).value.shouldBeCloseTo(0.0)
        c.isPossible(p5 ).value.shouldBeCloseTo(0.5)
        c.isPossible(p6 ).value.shouldBeCloseTo(0.0)
        c.isPossible(p7 ).value.shouldBeCloseTo(5.0 / 8)
        c.isPossible(p8 ).value.shouldBeCloseTo(1.0)
        c.isPossible(p9 ).value.shouldBeCloseTo(0.0)
        c.isPossible(p10).value.shouldBeCloseTo(0.0)
        c.isPossible(p11).value.shouldBeCloseTo(0.0)
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
        f.isNecessary(p0 ).value.shouldBeCloseTo(1.0 / 3)
        f.isNecessary(p1 ).value.shouldBeCloseTo(0.5)
        f.isNecessary(p2 ).value.shouldBeCloseTo(0.0)
        f.isNecessary(p3 ).value.shouldBeCloseTo(0.25)
        f.isNecessary(p4 ).value.shouldBeCloseTo(0.0)
        f.isNecessary(p5 ).value.shouldBeCloseTo(1.0 / 3)
        f.isNecessary(p6 ).value.shouldBeCloseTo(0.0)
        f.isNecessary(p7 ).value.shouldBeCloseTo(0.5)
        f.isNecessary(p8 ).value.shouldBeCloseTo(0.0)
        f.isNecessary(p9 ).value.shouldBeCloseTo(0.0)
        f.isNecessary(p10).value.shouldBeCloseTo(0.0)
        f.isNecessary(p11).value.shouldBeCloseTo(0.0)

        val c = Point.xy(0.0, 1.0)
        c.isNecessary(p0 ).value.shouldBeCloseTo(1.0)
        c.isNecessary(p1 ).value.shouldBeCloseTo(1.0)
        c.isNecessary(p2 ).value.shouldBeCloseTo(0.0)
        c.isNecessary(p3 ).value.shouldBeCloseTo(0.5)
        c.isNecessary(p4 ).value.shouldBeCloseTo(0.0)
        c.isNecessary(p5 ).value.shouldBeCloseTo(0.5)
        c.isNecessary(p6 ).value.shouldBeCloseTo(0.0)
        c.isNecessary(p7 ).value.shouldBeCloseTo(5.0 / 8)
        c.isNecessary(p8 ).value.shouldBeCloseTo(1.0)
        c.isNecessary(p9 ).value.shouldBeCloseTo(0.0)
        c.isNecessary(p10).value.shouldBeCloseTo(0.0)
        c.isNecessary(p11).value.shouldBeCloseTo(0.0)
    }

    @Test
    fun testDivide() {
        println("Divide")
        val c0 = Point.x(1.0)
        val c1 = Point.x(2.0)
        val f0 = Point.xr(1.0, 10.0)
        val f1 = Point.xr(2.0, 10.0)
        val f2 = Point.xr(2.0, 20.0)

        c0.lerp(0.3, c1).shouldEqualToPoint(Point.xr(1.3, 0.0))
        c0.lerp(-1.0, c1).shouldEqualToPoint(Point.xr(0.0, 0.0))
        c0.lerp(2.0, c1).shouldEqualToPoint(Point.xr(3.0, 0.0))
        c0.lerp(0.0, c1).shouldEqualToPoint(Point.xr(1.0, 0.0))
        c0.lerp(1.0, c1).shouldEqualToPoint(Point.xr(2.0, 0.0))

        f0.lerp(0.3, c1).shouldEqualToPoint(Point.xr(1.3, 7.0))
        f0.lerp(-1.0, c1).shouldEqualToPoint(Point.xr(0.0, 20.0))
        f0.lerp(2.0, c1).shouldEqualToPoint(Point.xr(3.0, 10.0))
        f0.lerp(0.0, c1).shouldEqualToPoint(Point.xr(1.0, 10.0))
        f0.lerp(1.0, c1).shouldEqualToPoint(Point.xr(2.0, 0.0))

        c0.lerp(0.3, f1).shouldEqualToPoint(Point.xr(1.3, 3.0))
        c0.lerp(-1.0, f1).shouldEqualToPoint(Point.xr(0.0, 10.0))
        c0.lerp(2.0, f1).shouldEqualToPoint(Point.xr(3.0, 20.0))
        c0.lerp(0.0, f1).shouldEqualToPoint(Point.xr(1.0, 0.0))
        c0.lerp(1.0, f1).shouldEqualToPoint(Point.xr(2.0, 10.0))

        f0.lerp(0.3, f2).shouldEqualToPoint(Point.xr(1.3, 13.0))
        f0.lerp(-1.0, f2).shouldEqualToPoint(Point.xr(0.0, 40.0))
        f0.lerp(2.0, f2).shouldEqualToPoint(Point.xr(3.0, 50.0))
        f0.lerp(0.0, f2).shouldEqualToPoint(Point.xr(1.0, 10.0))
        f0.lerp(1.0, f2).shouldEqualToPoint(Point.xr(2.0, 20.0))
    }

    @Test
    fun testToVector() {
        println("ToVector")
        f.toVector().shouldEqualToVector(Vector(1.0, -2.0, 3.0))
        c.toVector().shouldEqualToVector(Vector(1.0, -2.0, 3.0))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        f.toCrisp().shouldEqualToPoint(Point.xyz(1.0, -2.0, 3.0))
        c.toCrisp().shouldEqualToPoint(Point.xyz(1.0, -2.0, 3.0))
    }

    @Test
    fun testToArray() {
        println("ToArray")
        val a = f.toDoubleArray()
        a[0].shouldBeCloseTo( 1.0)
        a[1].shouldBeCloseTo(-2.0)
        a[2].shouldBeCloseTo( 3.0)
    }

    @Test
    fun testToString() {
        println("ToString")
        f.toString().parseJson().tryMap { Point.fromJson(it) }.orThrow().shouldEqualToPoint(f)
        c.toString().parseJson().tryMap { Point.fromJson(it) }.orThrow().shouldEqualToPoint(c)
    }

    @Test
    fun testPlus() {
        println("Plus")
        val p0 = Point.xyz(-1.0, -2.0, 4.0) + Vector(1.0, -2.0, 3.0)
        p0.shouldEqualToPoint(Point.xyz(0.0, -4.0, 7.0))
        val p1 = Point.xyz(-1.0, -2.0, 4.0) - Vector(1.0, -2.0, 3.0)
        p1.shouldEqualToPoint(Point.xyz(-2.0, 0.0, 1.0))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val p = Point.xyz(-1.0, -2.0, 4.0) - Point.xyz(1.0, -2.0, 3.0)
        p.shouldEqualToVector(Vector(-2.0, 0.0, 1.0))
    }

    @Test
    fun testDist() {
        println("Dist")
        val dp = Point.xyz(-1.0, -2.0, 4.0).dist(Point.xyz(1.0, -2.0, 3.0))
        dp.shouldBeCloseTo(FastMath.sqrt(5.0))
        val dl = Point.xyz(1.0, -1.0, 0.0).dist(Line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        dl.shouldBeCloseTo(12/5.0)
        val dplane = Point.xyz(1.0, -1.0, -3.0).dist(Plane(Point.xyz(1.0, -1.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        dplane.shouldBeCloseTo(3.0)
    }

    @Test
    fun testDistSquare() {
        println("DistSquare")
        val dp = Point.xyz(-1.0, -2.0, 4.0).distSquare(Point.xyz(1.0, -2.0, 3.0))
        dp.shouldBeCloseTo(5.0)
        val dl = Point.xyz(1.0, -1.0, 0.0).distSquare(Line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        dl.shouldBeCloseTo(144/25.0)
        val dplane = Point.xyz(1.0, -1.0, -3.0).distSquare(Plane(Point.xyz(2.0, -2.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        dplane.shouldBeCloseTo(9.0)
    }

    @Test
    fun testProjectTo() {
        println("ProjectTo")
        val pl = Point.xyz(1.0, -1.0, 0.0).projectTo(Line(Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 3.0, 0.0)))
        pl.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 0.0))
        val pp = Point.xyz(1.0, -1.0, -3.0).projectTo(Plane(Point.xyz(1.0, -1.0, 0.0), Point.xyz(-3.0, -1.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
        pp.shouldEqualToPoint(Point.xyz(1.0, -1.0, 0.0))
    }

    @Test
    fun testArea() {
        println("Area")
        val a = Point.xyz(1.0, 1.0, -1.0).area(Point.xyz(-3.0, 1.0, 2.0), Point.xyz(1.0, 4.0, -1.0))
        a.shouldBeCloseTo(7.5)
    }

    @Test
    fun testVolume() {
        println("Volume")
        val v = Point.xyz(0.0, 0.0, 0.0).volume(Point.xyz(1.0, 1.0, 0.0), Point.xyz(-1.0, 1.0, 0.0), Point.xyz(1.0, 1.0, -1.0))
        v.shouldBeCloseTo(1.0/3.0)
    }

    @Test
    fun testNormal() {
        println("Normal")
        val n = Point.xyz(1.0, 1.0, 0.0).normal(Point.xyz(-1.0, 1.0, 0.0), Point.xyz(0.0, 1.0, 1.0))
        n.orThrow().shouldEqualToVector(Vector(0.0, 1.0, 0.0))
    }

    @Test
    fun testTransform() {
        println("Affine")
        val t = Point.xyz(3.3, -2.4, -1.0).transform(Translate(Vector(2.3, -5.4, -0.5)))
        t.shouldEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = Point.xyz(1.0, 1.0, -1.0).transform(Rotate(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0))
        r.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = Point.xyz(3.0, -2.0, -1.0).transform(UniformlyScale(0.5))
        s.shouldEqualToPoint(Point.xyz(1.5, -1.0, -0.5))
    }
}