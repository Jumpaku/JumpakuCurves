package jumpaku.core.curve.bezier

import io.vavr.API
import jumpaku.core.affine.*
import jumpaku.core.json.parseToJson
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.junit.Test

class BezierTest {

    @Test
    fun testProperties() {
        println("Properties")
        val b4 = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
        pointAssertThat(b4.controlPoints[0]).isEqualToPoint(Point.xyr(-2.0, 0.0, 1.0))
        pointAssertThat(b4.controlPoints[1]).isEqualToPoint(Point.xyr(-1.0, 0.0, 2.0))
        pointAssertThat(b4.controlPoints[2]).isEqualToPoint(Point.xyr( 0.0, 2.0, 0.0))
        pointAssertThat(b4.controlPoints[3]).isEqualToPoint(Point.xyr( 1.0, 0.0, 2.0))
        pointAssertThat(b4.controlPoints[4]).isEqualToPoint(Point.xyr( 2.0, 0.0, 1.0))
        assertThat(b4.controlPoints.size()).isEqualTo(5)
        assertThat(b4.degree).isEqualTo(4)
        assertThat(b4.domain.begin).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(b4.domain.end).isEqualTo(1.0, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val p = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
        bezierAssertThat(p.toString().parseToJson().get().bezier).isEqualToBezier(p)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val b4 = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))

        pointAssertThat(b4.evaluate(0.0)).isEqualToPoint(Point.xyr(-2.0, 0.0      , 1.0        ))
        pointAssertThat(b4.evaluate(0.25)).isEqualToPoint(Point.xyr(-1.0, 27 / 64.0, 161.0 / 128))
        pointAssertThat(b4.evaluate(0.5)).isEqualToPoint(Point.xyr( 0.0, 0.75     , 9.0 / 8    ))
        pointAssertThat(b4.evaluate(0.75)).isEqualToPoint(Point.xyr( 1.0, 27 / 64.0, 161.0 / 128))
        pointAssertThat(b4.evaluate(1.0)).isEqualToPoint(Point.xyr( 2.0, 0.0      , 1.0        ))
    }

    @Test
    fun testDifferentiate() {
        val b = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
        val d = b.derivative
        bezierAssertThat(d.toBezier()).isEqualToBezier(Bezier(Point.xy(4.0, 0.0), Point.xy(4.0, 8.0), Point.xy(4.0, -8.0), Point.xy(4.0, 0.0)))
        vectorAssertThat(b.differentiate(0.0)).isEqualToVector(Vector(4.0, 0.0))
        vectorAssertThat(b.differentiate(0.25)).isEqualToVector(Vector(4.0, 2.25))
        vectorAssertThat(b.differentiate(0.5)).isEqualToVector(Vector(4.0, 0.0))
        vectorAssertThat(b.differentiate(0.75)).isEqualToVector(Vector(4.0, -2.25))
        vectorAssertThat(b.differentiate(1.0)).isEqualToVector(Vector(4.0, 0.0))
        vectorAssertThat(d.evaluate(0.0)).isEqualToVector(Vector(4.0, 0.0))
        vectorAssertThat(d.evaluate(0.25)).isEqualToVector(Vector(4.0, 2.25))
        vectorAssertThat(d.evaluate(0.5)).isEqualToVector(Vector(4.0, 0.0))
        vectorAssertThat(d.evaluate(0.75)).isEqualToVector(Vector(4.0, -2.25))
        vectorAssertThat(d.evaluate(1.0)).isEqualToVector(Vector(4.0, 0.0))
    }

    @Test
    fun testCrispTransform() {
        print("CrispTransform")
        val b = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
        val a = b.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).andTranslate(Vector(1.0, 1.0, 0.0)))
        val e = Bezier(Point.xy(1.0, -3.0), Point.xy(1.0, -1.0), Point.xy(-3.0, 1.0), Point.xy(1.0, 3.0), Point.xy(1.0, 5.0))
        bezierAssertThat(a).isEqualToBezier(e)
    }
    @Test
    fun testRestrict() {
        println("Restrict")
        val b = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
                .restrict(0.25, 0.5)
        bezierAssertThat(b).isEqualToBezier(Bezier(
                Point.xyr(-1.0, 27 / 64.0, 161 / 128.0), Point.xyr(-3 / 4.0, 9 / 16.0, 39 / 32.0), Point.xyr(-1 / 2.0, 11 / 16.0, 37 / 32.0), Point.xyr(-1 / 4.0, 3 / 4.0, 9 / 8.0), Point.xyr(0.0, 3 / 4.0, 9 / 8.0)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
                .reverse()
        bezierAssertThat(r).isEqualToBezier(Bezier(
                Point.xyr(2.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(-1.0, 0.0, 2.0), Point.xyr(-2.0, 0.0, 1.0)))
    }

    @Test
    fun testElevate() {
        println("Elevate")
        val instance = Bezier(Point.xr(-1.0, 0.0), Point.xr(0.0, 2.0), Point.xr(1.0, 0.0))
        val expected = Bezier(Point.xr(-1.0, 0.0), Point.xr(-1 / 3.0, 4 / 3.0), Point.xr(1 / 3.0, 4 / 3.0), Point.xr(1.0, 0.0))
        bezierAssertThat(instance.elevate()).isEqualToBezier(expected)
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val b1 = Bezier(Point.xyr(-1.0, 2.0, 2.0), Point.xyr(1.0, 1.0, 1.0))
                .reduce()
        val e1 = Bezier(Point.xyr(0.0, 1.5, 1.5))
        bezierAssertThat(b1).isEqualToBezier(e1)

        val b2 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(0.0, 2.0, 2.0), Point.xyr(1.0, 0.0, 0.0))
                .reduce()
        val e2 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(1.0, 0.0, 0.0))
        bezierAssertThat(b2).isEqualToBezier(e2)

        val b3 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1.0, 0.0, 0.0)).reduce()
        val e3 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(0.0, 2.0, 2.0), Point.xyr(1.0, 0.0, 0.0))
        bezierAssertThat(b3).isEqualToBezier(e3)

        val b4 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-0.5, 1.0, 1.0), Point.xyr(0.0, 4 / 3.0, 4 / 3.0), Point.xyr(0.5, 1.0, 1.0), Point.xyr(1.0, 0.0, 0.0))
                .reduce()
        val e4 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1 / 3.0, 4 / 3.0, 4 / 3.0), Point.xyr(1.0, 0.0, 0.0))
        bezierAssertThat(b4).isEqualToBezier(e4)

        val b5 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-0.6, 0.8, 0.8), Point.xyr(-0.3, 1.2, 1.2), Point.xyr(0.3, 1.2, 1.2), Point.xyr(0.6, 0.8, 0.8), Point.xyr(1.0, 0.0, 0.0))
                .reduce()
        val e5 = Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-0.5, 1.0, 1.0), Point.xyr(0.0, 4 / 3.0, 8 / 3.0), Point.xyr(0.5, 1.0, 1.0), Point.xyr(1.0, 0.0, 0.0))
        bezierAssertThat(b5).isEqualToBezier(e5)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val bs = Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))
                .subdivide(0.25)
        bezierAssertThat(bs._1()).isEqualToBezier(Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-7 / 4.0, 0.0, 5 / 4.0), Point.xyr(-3 / 2.0, 1 / 8.0, 21 / 16.0), Point.xyr(-5 / 4.0, 9 / 32.0, 83 / 64.0), Point.xyr(-1.0, 27 / 64.0, 161 / 128.0)))
        bezierAssertThat(bs._2()).isEqualToBezier(Bezier(
                Point.xyr(-1.0, 27 / 64.0, 322 / 256.0), Point.xyr(-1 / 4.0, 27 / 32.0, 73 / 64.0), Point.xyr(1 / 2.0, 9 / 8.0, 13 / 16.0), Point.xyr(5 / 4.0, 0.0, 7 / 4.0), Point.xyr(2.0, 0.0, 1.0)))
    }

    @Test
    fun testExtend() {
        println("Extend")
        val extendBack = Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-7 / 4.0, 0.0, 5 / 4.0), Point.xyr(-3 / 2.0, 1 / 8.0, 21 / 16.0), Point.xyr(-5 / 4.0, 9 / 32.0, 83 / 64.0), Point.xyr(-1.0, 27 / 64.0, 161 / 128.0))
                .extend(4.0)
        bezierAssertThat(extendBack).isEqualToBezier(Bezier(
                Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 8.0), Point.xyr(0.0, 2.0, 60.0), Point.xyr(1.0, 0.0, 434.0), Point.xyr(2.0, 0.0, 3073.0)))

        val extendFront = Bezier(
                Point.xyr(-1.0, 27 / 64.0, 322 / 256.0), Point.xyr(-1 / 4.0, 27 / 32.0, 73 / 64.0), Point.xyr(1 / 2.0, 9 / 8.0, 13 / 16.0), Point.xyr(5 / 4.0, 0.0, 7 / 4.0), Point.xyr(2.0, 0.0, 1.0))
                .extend(-1/3.0)
        bezierAssertThat(extendFront).isEqualToBezier(Bezier(
                Point.xyr(-2.0, 0.0, 721 / 81.0), Point.xyr(-1.0, 0.0, 134 / 27.0), Point.xyr(0.0, 2.0, 28 / 9.0), Point.xyr(1.0, 0.0, 8 / 3.0), Point.xyr(2.0, 0.0, 1.0)))

    }

    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        val l = Bezier(Point.xy(0.0, 0.0), Point.xy(50.0, 0.0), Point.xy(100.0, 100.0)).toArcLengthCurve().arcLength()
        assertThat(l).isEqualTo(7394.71429/50, withPrecision(0.1))
    }

    @Test
    fun testDecasteljau(){
        println("Decasteljau")
        val result = Bezier.decasteljau(0.25,
                API.Array(Point.xyzr(1.0, 0.0, -2.0, 1.0) as Point, Point.xyzr(0.0, 3.0, 4.0, 0.0), Point.xyzr(-1.0, -1.0, 0.0, 2.0)))
        assertThat(result.size()).isEqualTo(result.size())
        pointAssertThat(result.get(0)).isEqualToPoint(Point.xyzr(0.75, 0.75, -0.5, 0.75))
        pointAssertThat(result.get(1)).isEqualToPoint(Point.xyzr(-0.25, 2.0, 3.0, 0.5))
    }

    @Test
    fun test_Basis(){
        print("Basis")
        assertThat(Bezier.basis(2, 0, 0.0 )).isEqualTo(1.0,      withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 1, 0.0 )).isEqualTo(0.0,      withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 2, 0.0 )).isEqualTo(0.0,      withPrecision(1.0e-10))

        assertThat(Bezier.basis(2, 0, 0.25)).isEqualTo(9 / 16.0, withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 1, 0.25)).isEqualTo(6 / 16.0, withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 2, 0.25)).isEqualTo(1 / 16.0, withPrecision(1.0e-10))

        assertThat(Bezier.basis(2, 0, 0.5 )).isEqualTo(0.25,     withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 1, 0.5 )).isEqualTo(0.5,      withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 2, 0.5 )).isEqualTo(0.25,     withPrecision(1.0e-10))

        assertThat(Bezier.basis(2, 0, 0.75)).isEqualTo(1 / 16.0, withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 1, 0.75)).isEqualTo(6 / 16.0, withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 2, 0.75)).isEqualTo(9 / 16.0, withPrecision(1.0e-10))

        assertThat(Bezier.basis(2, 0, 1.0 )).isEqualTo(0.0,      withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 1, 1.0 )).isEqualTo(0.0,      withPrecision(1.0e-10))
        assertThat(Bezier.basis(2, 2, 1.0 )).isEqualTo(1.0,      withPrecision(1.0e-10))
    }
}