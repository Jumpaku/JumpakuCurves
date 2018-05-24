package jumpaku.fsc.test.classify

import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point
import jumpaku.core.json.parseJson
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.core.util.hashMap
import jumpaku.fsc.classify.ClassifyResult
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.classify.References
import org.amshove.kluent.shouldBe
import org.junit.Test

class ClassifyResultTest {

    val s = hashMap(
            CurveClass.Point to Grade(0.3),
            CurveClass.LineSegment to Grade(0.7),
            CurveClass.Circle to Grade(0.4),
            CurveClass.CircularArc to Grade(0.0),
            CurveClass.Ellipse to Grade(0.9),
            CurveClass.EllipticArc to Grade(0.3),
            CurveClass.ClosedFreeCurve to Grade(0.5),
            CurveClass.OpenFreeCurve to Grade(0.8))
    val rs = References(
            ConicSection(Point.x(0.0), Point.x(1.0), Point.x(2.0), 1.0),
            ConicSection(Point.x(0.0), Point.xy(1.0, 1.0), Point.x(2.0), 0.0),
            ConicSection(Point.x(0.0), Point.xy(0.0, 1.0), Point.x(2.0), 0.0))

    val r = ClassifyResult(s, rs)

    @Test
    fun testProperties() {
        println("Properties")
        r.curveClass.shouldBe(CurveClass.Ellipse)
        r.grade.value.shouldBeCloseTo(0.9)
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = r.toString().parseJson().flatMap { ClassifyResult.fromJson(it) }.get()
        a.shouldEqualToClassifyResult(r)
    }
}