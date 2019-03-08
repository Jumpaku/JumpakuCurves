package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.IdentifyResult
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class IdentifyResultTest {

    val r2 = sqrt(2.0)

    val s = hashMapOf(
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
            ConicSection(Point.xy(-r2 / 2, -r2 / 2), Point.xy(0.0, 1.0), Point.xy(r2 / 2, -r2 / 2), -r2 / 2),
            Interval(-0.5, 1.5))

    val e = Reference(
            ConicSection(Point.xy(-r2 / 2, -r2 / 2), Point.xy(-r2 / 2, 1.0), Point.xy(r2 / 2, -r2 / 2), -r2 / 2),
            Interval(-0.5, 1.5))

    val r = IdentifyResult(s, l, c, e)

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(r.curveClass, `is`(CurveClass.Ellipse))
        assertThat(r.grade.value, `is`(greaterThan(0.5)))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = r.toString().parseJson().tryMap { IdentifyResult.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(r)))
    }
}