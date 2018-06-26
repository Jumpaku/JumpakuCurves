package jumpaku.fsc.test.identify

import jumpaku.fsc.identify.CurveClass
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

class CurveClassTest {
    @Test
    fun testIsFreeCurve() {
        println("IsFreeCurve")
        CurveClass.Point.isFreeCurve.shouldBeFalse()
        CurveClass.LineSegment.isFreeCurve.shouldBeFalse()
        CurveClass.Circle.isFreeCurve.shouldBeFalse()
        CurveClass.CircularArc.isFreeCurve.shouldBeFalse()
        CurveClass.Ellipse.isFreeCurve.shouldBeFalse()
        CurveClass.EllipticArc.isFreeCurve.shouldBeFalse()
        CurveClass.ClosedFreeCurve.isFreeCurve.shouldBeTrue()
        CurveClass.OpenFreeCurve.isFreeCurve.shouldBeTrue()
    }

    @Test
    fun testIsConicSection() {
        println("IsConicSection")
        CurveClass.Point.isLinear.shouldBeTrue()
        CurveClass.LineSegment.isConicSection.shouldBeTrue()
        CurveClass.Circle.isConicSection.shouldBeTrue()
        CurveClass.CircularArc.isConicSection.shouldBeTrue()
        CurveClass.Ellipse.isConicSection.shouldBeTrue()
        CurveClass.EllipticArc.isConicSection.shouldBeTrue()
        CurveClass.ClosedFreeCurve.isConicSection.shouldBeFalse()
        CurveClass.OpenFreeCurve.isConicSection.shouldBeFalse()
    }

    @Test
    fun testIsOpen() {
        println("IsOpen")
        CurveClass.Point.isOpen.shouldBeFalse()
        CurveClass.LineSegment.isOpen.shouldBeTrue()
        CurveClass.Circle.isOpen.shouldBeFalse()
        CurveClass.CircularArc.isOpen.shouldBeTrue()
        CurveClass.Ellipse.isOpen.shouldBeFalse()
        CurveClass.EllipticArc.isOpen.shouldBeTrue()
        CurveClass.ClosedFreeCurve.isOpen.shouldBeFalse()
        CurveClass.OpenFreeCurve.isOpen.shouldBeTrue()
    }

    @Test
    fun testIsClosed() {
        println("IsClosed")
        CurveClass.Point.isClosed.shouldBeTrue()
        CurveClass.LineSegment.isClosed.shouldBeFalse()
        CurveClass.Circle.isClosed.shouldBeTrue()
        CurveClass.CircularArc.isClosed.shouldBeFalse()
        CurveClass.Ellipse.isClosed.shouldBeTrue()
        CurveClass.EllipticArc.isClosed.shouldBeFalse()
        CurveClass.ClosedFreeCurve.isClosed.shouldBeTrue()
        CurveClass.OpenFreeCurve.isClosed.shouldBeFalse()
    }

    @Test
    fun testIsElliptic() {
        println("IsElliptic")
        CurveClass.Point.isElliptic.shouldBeFalse()
        CurveClass.LineSegment.isElliptic.shouldBeFalse()
        CurveClass.Circle.isElliptic.shouldBeFalse()
        CurveClass.CircularArc.isElliptic.shouldBeFalse()
        CurveClass.Ellipse.isElliptic.shouldBeTrue()
        CurveClass.EllipticArc.isElliptic.shouldBeTrue()
        CurveClass.ClosedFreeCurve.isElliptic.shouldBeFalse()
        CurveClass.OpenFreeCurve.isElliptic.shouldBeFalse()
    }

    @Test
    fun testIsCircular() {
        println("IsCircular")
        CurveClass.Point.isCircular.shouldBeFalse()
        CurveClass.LineSegment.isCircular.shouldBeFalse()
        CurveClass.Circle.isCircular.shouldBeTrue()
        CurveClass.CircularArc.isCircular.shouldBeTrue()
        CurveClass.Ellipse.isCircular.shouldBeFalse()
        CurveClass.EllipticArc.isCircular.shouldBeFalse()
        CurveClass.ClosedFreeCurve.isCircular.shouldBeFalse()
        CurveClass.OpenFreeCurve.isCircular.shouldBeFalse()
    }

    @Test
    fun testIsLinear() {
        println("IsLinear")
        CurveClass.Point.isLinear.shouldBeTrue()
        CurveClass.LineSegment.isLinear.shouldBeTrue()
        CurveClass.Circle.isLinear.shouldBeFalse()
        CurveClass.CircularArc.isLinear.shouldBeFalse()
        CurveClass.Ellipse.isLinear.shouldBeFalse()
        CurveClass.EllipticArc.isLinear.shouldBeFalse()
        CurveClass.ClosedFreeCurve.isLinear.shouldBeFalse()
        CurveClass.OpenFreeCurve.isLinear.shouldBeFalse()
    }

}