package jumpaku.fsc.classify

import org.assertj.core.api.Assertions.*
import jumpaku.core.fuzzy.Grade
import org.junit.Test


class ClassifyResultTest {

    @Test
    fun testProperties() {
        println("Properties")
        val s = arrayOf(
                CurveClass.Point to Grade(0.3),
                CurveClass.LineSegment to Grade(0.7),
                CurveClass.Circle to Grade(0.4),
                CurveClass.CircularArc to Grade(0.0),
                CurveClass.Ellipse to Grade(0.9),
                CurveClass.EllipticArc to Grade(0.3),
                CurveClass.ClosedFreeCurve to Grade(0.5),
                CurveClass.OpenFreeCurve to Grade(0.8))

        val r = ClassifyResult(*s)

        assertThat(r.curveClass).isEqualTo(CurveClass.Ellipse)
        assertThat(r.grade.value).isEqualTo(0.9, withPrecision(1.0e-10))
    }

    /*@Test
    fun testToString() {
        println("ToString")
        val s = arrayOf(
                CurveClass.Point to Grade(0.3),
                CurveClass.LineSegment to Grade(0.7),
                CurveClass.Circle to Grade(0.4),
                CurveClass.CircularArc to Grade(0.0),
                CurveClass.Ellipse to Grade(0.9),
                CurveClass.EllipticArc to Grade(0.3),
                CurveClass.ClosedFreeCurve to Grade(0.5),
                CurveClass.OpenFreeCurve to Grade(0.8))
        val r = ClassifyResult(*s)

        assertThat(prettyGson.fromJson<ClassifyResultJson>(r.toString()).classifyResult()).isEqualTo(r)
    }*/
}