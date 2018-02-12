package jumpaku.fsc.classify

import org.assertj.core.api.Assertions.*
import org.junit.Test

class CurveClassTest {
    @Test
    fun testIsFreeCurve() {
        println("IsFreeCurve")
        assertThat(CurveClass.Point.isFreeCurve).isFalse()
        assertThat(CurveClass.LineSegment.isFreeCurve).isFalse()
        assertThat(CurveClass.Circle.isFreeCurve).isFalse()
        assertThat(CurveClass.CircularArc.isFreeCurve).isFalse()
        assertThat(CurveClass.Ellipse.isFreeCurve).isFalse()
        assertThat(CurveClass.EllipticArc.isFreeCurve).isFalse()
        assertThat(CurveClass.ClosedFreeCurve.isFreeCurve).isTrue()
        assertThat(CurveClass.OpenFreeCurve.isFreeCurve).isTrue()
    }

    @Test
    fun testIsConicSection() {
        println("IsConicSection")
        assertThat(CurveClass.Point.isLinear).isTrue()
        assertThat(CurveClass.LineSegment.isConicSection).isTrue()
        assertThat(CurveClass.Circle.isConicSection).isTrue()
        assertThat(CurveClass.CircularArc.isConicSection).isTrue()
        assertThat(CurveClass.Ellipse.isConicSection).isTrue()
        assertThat(CurveClass.EllipticArc.isConicSection).isTrue()
        assertThat(CurveClass.ClosedFreeCurve.isConicSection).isFalse()
        assertThat(CurveClass.OpenFreeCurve.isConicSection).isFalse()
    }

    @Test
    fun testIsOpen() {
        println("IsOpen")
        assertThat(CurveClass.Point.isOpen).isFalse()
        assertThat(CurveClass.LineSegment.isOpen).isTrue()
        assertThat(CurveClass.Circle.isOpen).isFalse()
        assertThat(CurveClass.CircularArc.isOpen).isTrue()
        assertThat(CurveClass.Ellipse.isOpen).isFalse()
        assertThat(CurveClass.EllipticArc.isOpen).isTrue()
        assertThat(CurveClass.ClosedFreeCurve.isOpen).isFalse()
        assertThat(CurveClass.OpenFreeCurve.isOpen).isTrue()
    }

    @Test
    fun testIsClosed() {
        println("IsClosed")
        assertThat(CurveClass.Point.isClosed).isTrue()
        assertThat(CurveClass.LineSegment.isClosed).isFalse()
        assertThat(CurveClass.Circle.isClosed).isTrue()
        assertThat(CurveClass.CircularArc.isClosed).isFalse()
        assertThat(CurveClass.Ellipse.isClosed).isTrue()
        assertThat(CurveClass.EllipticArc.isClosed).isFalse()
        assertThat(CurveClass.ClosedFreeCurve.isClosed).isTrue()
        assertThat(CurveClass.OpenFreeCurve.isClosed).isFalse()
    }

    @Test
    fun testIsElliptic() {
        println("IsElliptic")
        assertThat(CurveClass.Point.isElliptic).isFalse()
        assertThat(CurveClass.LineSegment.isElliptic).isFalse()
        assertThat(CurveClass.Circle.isElliptic).isFalse()
        assertThat(CurveClass.CircularArc.isElliptic).isFalse()
        assertThat(CurveClass.Ellipse.isElliptic).isTrue()
        assertThat(CurveClass.EllipticArc.isElliptic).isTrue()
        assertThat(CurveClass.ClosedFreeCurve.isElliptic).isFalse()
        assertThat(CurveClass.OpenFreeCurve.isElliptic).isFalse()
    }

    @Test
    fun testIsCircular() {
        println("IsCircular")
        assertThat(CurveClass.Point.isCircular).isFalse()
        assertThat(CurveClass.LineSegment.isCircular).isFalse()
        assertThat(CurveClass.Circle.isCircular).isTrue()
        assertThat(CurveClass.CircularArc.isCircular).isTrue()
        assertThat(CurveClass.Ellipse.isCircular).isFalse()
        assertThat(CurveClass.EllipticArc.isCircular).isFalse()
        assertThat(CurveClass.ClosedFreeCurve.isCircular).isFalse()
        assertThat(CurveClass.OpenFreeCurve.isCircular).isFalse()
    }

    @Test
    fun testIsLinear() {
        println("IsLinear")
        assertThat(CurveClass.Point.isLinear).isTrue()
        assertThat(CurveClass.LineSegment.isLinear).isTrue()
        assertThat(CurveClass.Circle.isLinear).isFalse()
        assertThat(CurveClass.CircularArc.isLinear).isFalse()
        assertThat(CurveClass.Ellipse.isLinear).isFalse()
        assertThat(CurveClass.EllipticArc.isLinear).isFalse()
        assertThat(CurveClass.ClosedFreeCurve.isLinear).isFalse()
        assertThat(CurveClass.OpenFreeCurve.isLinear).isFalse()
    }

}