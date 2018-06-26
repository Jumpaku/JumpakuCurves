package jumpaku.fsc.test.identify

import jumpaku.core.curve.Interval
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point
import jumpaku.core.json.parseJson
import jumpaku.core.util.hashMap
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.identify.IdentifyResult
import jumpaku.fsc.identify.reference.Reference
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Test
import kotlin.math.sqrt

class IdentifyResultTest {

    val r2 = sqrt(2.0)

    val s = hashMap(
            CurveClass.Point to Grade(0.3),
            CurveClass.LineSegment to Grade(0.7),
            CurveClass.Circle to Grade(0.4),
            CurveClass.CircularArc to Grade(0.0),
            CurveClass.Ellipse to Grade(0.9),
            CurveClass.EllipticArc to Grade(0.3),
            CurveClass.ClosedFreeCurve to Grade(0.5),
            CurveClass.OpenFreeCurve to Grade(0.8))

    val l = Reference(
            ConicSection.lineSegment(Point.x(-1.0), Point.x(1.0)),
            Interval(-0.25, 1.25))


    val c = Reference(
            ConicSection(Point.xy(-r2/2, -r2/2), Point.xy(0.0, 1.0), Point.xy(r2/2, -r2/2), -r2/2),
            Interval(-0.5, 1.5))

    val e = Reference(
            ConicSection(Point.xy(-r2/2, -r2/2), Point.xy(-r2/2, 1.0), Point.xy(r2/2, -r2/2), -r2/2),
            Interval(-0.5, 1.5))

    val r = IdentifyResult(s, l, c, e)

    @Test
    fun testProperties() {
        println("Properties")
        r.curveClass.shouldBe(CurveClass.Ellipse)
        r.grade.value.shouldBeGreaterThan(0.5)
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = r.toString().parseJson().flatMap { IdentifyResult.fromJson(it) }.get()
        a.shouldEqualToClassifyResult(r)
    }
}