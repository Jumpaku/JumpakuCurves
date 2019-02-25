package jumpaku.curves.core.test.curve.polyline

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import jumpaku.curves.core.test.shouldBeCloseTo
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class PolylineTest {

    val pl = Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))

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
        pl.toString().parseJson().tryMap { Polyline.fromJson(it) }.orThrow().shouldEqualToPolyline(pl)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val p = Polyline.byArcLength(Point.xyr(1.0, 1.0, 2.0), Point.xyr(-1.0, -1.0, 1.0), Point.xyzr(-1.0, -1.0, 1.0, 0.0))
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
        ps.size.shouldEqualTo(6)
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
        val e = Polyline.byArcLength(Point.xy(-1.0, -1.0), Point.xy(-1.0, 3.0), Point.xy(7.0, 3.0), Point.xyz(7.0, 3.0, 3.0))
        a.shouldEqualToPolyline(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        pl.toCrisp().shouldEqualToPolyline(
                Polyline.byArcLength(Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, -3.0), Point.xyz(1.0, -3.0, 1.5)))

    }

    @Test
    fun testReverse() {
        println("Reverse")
        pl.reverse().shouldEqualToPolyline(
                Polyline.byArcLength(Point.xyzr(1.0, -3.0, 1.5, 2.0), Point.xyr(1.0, -3.0, 3.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(-1.0, 1.0, 2.0)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        pl.restrict(3.0, 4.5).shouldEqualToPolyline(Polyline(listOf(ParamPoint(Point.xyr(1.0, 0.0, 1.5), 3.0), ParamPoint(Point.xyr(1.0, -1.5, 2.25), 4.5))))
        pl.restrict(Interval(3.0, 4.5)).shouldEqualToPolyline(Polyline(listOf(ParamPoint(Point.xyr(1.0, 0.0, 1.5), 3.0), ParamPoint(Point.xyr(1.0, -1.5, 2.25), 4.5))))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val ps0 = pl.subdivide(4.5)
        ps0._1().shouldEqualToPolyline(Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -1.5, 2.25)))
        ps0._2().shouldEqualToPolyline(Polyline.byArcLength(Point.xyr(1.0, -1.5, 2.25), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0)))

        val ps1 = pl.subdivide(0.0)
        ps1._1().shouldEqualToPolyline(Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0)))
        ps1._2().shouldEqualToPolyline(pl)

        val ps2 = pl.subdivide(7.5)
        ps2._1().shouldEqualToPolyline(pl)
        ps2._2().shouldEqualToPolyline(Polyline(listOf(ParamPoint(Point.xyzr(1.0, -3.0, 1.5, 2.0), 7.5))))

        val ps3 = pl.subdivide(2.0)
        ps3._1().shouldEqualToPolyline(Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0)))
        ps3._2().shouldEqualToPolyline(Polyline(listOf(
                ParamPoint(Point.xyr(1.0, 1.0, 1.0), 2.0),
                ParamPoint(Point.xyr(1.0, -3.0, 3.0), 6.0),
                ParamPoint(Point.xyzr(1.0, -3.0, 1.5, 2.0), 7.5))))

    }
}