package jumpaku.core.test.curve.polyline

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.transform.Rotate
import jumpaku.core.transform.Translate
import jumpaku.core.transform.UniformlyScale
import jumpaku.core.curve.Interval
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.shouldEqualToPoint
import jumpaku.core.test.shouldBeCloseTo
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class PolylineTest {

    val pl = Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))

    @Test
    fun testProperties() {
        println("Properties")
        pl.points[0].shouldEqualToPoint(Point.xyr(-1.0, 1.0, 2.0))
        pl.points[1].shouldEqualToPoint(Point.xyr( 1.0, 1.0, 1.0))
        pl.points[2].shouldEqualToPoint(Point.xyr( 1.0,-3.0, 3.0))
        pl.points[3].shouldEqualToPoint(Point.xyzr( 1.0,-3.0, 1.5, 2.0))
        pl.domain.begin.shouldBeCloseTo(0.0)
        pl.domain.end.shouldBeCloseTo(7.5)
    }

    @Test
    fun testToString() {
        println("ToString")
        pl.toString().parseJson().flatMap { Polyline.fromJson(it) }.get().shouldEqualToPolyline(pl)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val p = Polyline(Point.xyr(1.0, 1.0, 2.0), Point.xyr(-1.0, -1.0, 1.0), Point.xyzr(-1.0, -1.0, 1.0, 0.0))
        p.evaluate(0.0).shouldEqualToPoint(Point.xyzr( 1.0, 1.0, 0.0, 2.0))
        p.evaluate(Math.sqrt(2.0)).shouldEqualToPoint(Point.xyzr( 0.0, 0.0, 0.0, 1.5))
        p.evaluate(2 * Math.sqrt(2.0)).shouldEqualToPoint(Point.xyzr(-1.0,-1.0, 0.0, 1.0))
        p.evaluate(2 * Math.sqrt(2.0) + 0.5).shouldEqualToPoint(Point.xyzr(-1.0,-1.0, 0.5, 0.5))
        p.evaluate(2 * Math.sqrt(2.0) + 1).shouldEqualToPoint(Point.xyzr(-1.0,-1.0, 1.0, 0.0))
    }

    @Test
    fun testEvaluateAll() {
        println("EvaluateAll")
        val ps = pl.evaluateAll(6)
        ps.size().shouldEqualTo(6)
        ps[0].shouldEqualToPoint(Point.xyzr(-1.0, 1.0, 0.0, 2.0 ))
        ps[1].shouldEqualToPoint(Point.xyzr( 0.5, 1.0, 0.0, 1.25))
        ps[2].shouldEqualToPoint(Point.xyzr( 1.0, 0.0, 0.0, 1.5 ))
        ps[3].shouldEqualToPoint(Point.xyzr( 1.0,-1.5, 0.0, 2.25))
        ps[4].shouldEqualToPoint(Point.xyzr( 1.0,-3.0, 0.0, 3.0 ))
        ps[5].shouldEqualToPoint(Point.xyzr( 1.0,-3.0, 1.5, 2.0 ))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = pl.transform(UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
                .andThen(Translate(Vector(1.0, 1.0))))
        val e = Polyline(Point.xy(-1.0, -1.0), Point.xy(-1.0, 3.0), Point.xy(7.0, 3.0), Point.xyz(7.0, 3.0, 3.0))
        a.shouldEqualToPolyline(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        pl.toCrisp().shouldEqualToPolyline(
                Polyline(Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, -3.0), Point.xyz(1.0, -3.0, 1.5)))

    }

    @Test
    fun testReverse() {
        println("Reverse")
        pl.reverse().shouldEqualToPolyline(
                Polyline(Point.xyzr(1.0, -3.0, 1.5, 2.0), Point.xyr(1.0, -3.0, 3.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(-1.0, 1.0, 2.0)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        pl.restrict(3.0, 4.5).shouldEqualToPolyline(Polyline(Point.xyr(1.0, 0.0, 1.5), Point.xyr(1.0, -1.5, 2.25)))
        pl.restrict(Interval(3.0, 4.5)).shouldEqualToPolyline(Polyline(Point.xyr(1.0, 0.0, 1.5), Point.xyr(1.0, -1.5, 2.25)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val ps = pl.subdivide(4.5)
        ps._1().shouldEqualToPolyline(Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -1.5, 2.25)))
        ps._2().shouldEqualToPolyline(Polyline(Point.xyr(1.0, -1.5, 2.25), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0)))
    }

    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        val l = Polyline(
                Point.xyr(-100.0, 100.0, 2.0),
                Point.xyr(100.0, 100.0, 1.0),
                Point.xyr(100.0, -300.0, 3.0),
                Point.xyzr(100.0, -300.0, 150.0, 2.0))
                .reparametrizeArcLength().arcLength()
        l.shouldBeCloseTo(750.0, 0.1)
    }
}