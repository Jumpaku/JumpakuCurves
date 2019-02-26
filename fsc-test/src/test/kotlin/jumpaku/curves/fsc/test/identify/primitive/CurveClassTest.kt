package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.curves.fsc.identify.primitive.CurveClass
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class CurveClassTest {
    @Test
    fun testIsFreeCurve() {
        println("IsFreeCurve")
        assertThat(CurveClass.Point.isFreeCurve, `is`(false))
        assertThat(CurveClass.LineSegment.isFreeCurve, `is`(false))
        assertThat(CurveClass.Circle.isFreeCurve, `is`(false))
        assertThat(CurveClass.CircularArc.isFreeCurve, `is`(false))
        assertThat(CurveClass.Ellipse.isFreeCurve, `is`(false))
        assertThat(CurveClass.EllipticArc.isFreeCurve, `is`(false))
        assertThat(CurveClass.ClosedFreeCurve.isFreeCurve, `is`(true))
        assertThat(CurveClass.OpenFreeCurve.isFreeCurve, `is`(true))
    }

    @Test
    fun testIsConicSection() {
        println("IsConicSection")
        assertThat(CurveClass.Point.isLinear, `is`(true))
        assertThat(CurveClass.LineSegment.isConicSection, `is`(true))
        assertThat(CurveClass.Circle.isConicSection, `is`(true))
        assertThat(CurveClass.CircularArc.isConicSection, `is`(true))
        assertThat(CurveClass.Ellipse.isConicSection, `is`(true))
        assertThat(CurveClass.EllipticArc.isConicSection, `is`(true))
        assertThat(CurveClass.ClosedFreeCurve.isConicSection, `is`(false))
        assertThat(CurveClass.OpenFreeCurve.isConicSection, `is`(false))
    }

    @Test
    fun testIsOpen() {
        println("IsOpen")
        assertThat(CurveClass.Point.isOpen, `is`(false))
        assertThat(CurveClass.LineSegment.isOpen, `is`(true))
        assertThat(CurveClass.Circle.isOpen, `is`(false))
        assertThat(CurveClass.CircularArc.isOpen, `is`(true))
        assertThat(CurveClass.Ellipse.isOpen, `is`(false))
        assertThat(CurveClass.EllipticArc.isOpen, `is`(true))
        assertThat(CurveClass.ClosedFreeCurve.isOpen, `is`(false))
        assertThat(CurveClass.OpenFreeCurve.isOpen, `is`(true))
    }

    @Test
    fun testIsClosed() {
        println("IsClosed")
        assertThat(CurveClass.Point.isClosed, `is`(true))
        assertThat(CurveClass.LineSegment.isClosed, `is`(false))
        assertThat(CurveClass.Circle.isClosed, `is`(true))
        assertThat(CurveClass.CircularArc.isClosed, `is`(false))
        assertThat(CurveClass.Ellipse.isClosed, `is`(true))
        assertThat(CurveClass.EllipticArc.isClosed, `is`(false))
        assertThat(CurveClass.ClosedFreeCurve.isClosed, `is`(true))
        assertThat(CurveClass.OpenFreeCurve.isClosed, `is`(false))
    }

    @Test
    fun testIsElliptic() {
        println("IsElliptic")
        assertThat(CurveClass.Point.isElliptic, `is`(false))
        assertThat(CurveClass.LineSegment.isElliptic, `is`(false))
        assertThat(CurveClass.Circle.isElliptic, `is`(false))
        assertThat(CurveClass.CircularArc.isElliptic, `is`(false))
        assertThat(CurveClass.Ellipse.isElliptic, `is`(true))
        assertThat(CurveClass.EllipticArc.isElliptic, `is`(true))
        assertThat(CurveClass.ClosedFreeCurve.isElliptic, `is`(false))
        assertThat(CurveClass.OpenFreeCurve.isElliptic, `is`(false))
    }

    @Test
    fun testIsCircular() {
        println("IsCircular")
        assertThat(CurveClass.Point.isCircular, `is`(false))
        assertThat(CurveClass.LineSegment.isCircular, `is`(false))
        assertThat(CurveClass.Circle.isCircular, `is`(true))
        assertThat(CurveClass.CircularArc.isCircular, `is`(true))
        assertThat(CurveClass.Ellipse.isCircular, `is`(false))
        assertThat(CurveClass.EllipticArc.isCircular, `is`(false))
        assertThat(CurveClass.ClosedFreeCurve.isCircular, `is`(false))
        assertThat(CurveClass.OpenFreeCurve.isCircular, `is`(false))
    }

    @Test
    fun testIsLinear() {
        println("IsLinear")
        assertThat(CurveClass.Point.isLinear, `is`(true))
        assertThat(CurveClass.LineSegment.isLinear, `is`(true))
        assertThat(CurveClass.Circle.isLinear, `is`(false))
        assertThat(CurveClass.CircularArc.isLinear, `is`(false))
        assertThat(CurveClass.Ellipse.isLinear, `is`(false))
        assertThat(CurveClass.EllipticArc.isLinear, `is`(false))
        assertThat(CurveClass.ClosedFreeCurve.isLinear, `is`(false))
        assertThat(CurveClass.OpenFreeCurve.isLinear, `is`(false))
    }

}