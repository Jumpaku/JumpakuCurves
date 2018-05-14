package jumpaku.core.test.curve.arclength

import jumpaku.core.affine.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.test.affine.shouldEqualToPoint
import jumpaku.core.test.curve.rationalbezier.shouldEqualToConicSection
import jumpaku.core.test.curve.shouldEqualToInterval
import jumpaku.core.test.shouldBeCloseTo
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test


class ArcLengthReparametrizedTest {

    val R2 = FastMath.sqrt(2.0)

    val PI = FastMath.PI

    @Test
    fun testProperties() {
        println("Properties")
        val c = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 1.0), Point.xyr(1.0, 0.0, 1.0), R2 / 2)
        val a = ArcLengthReparametrized(c, 101)
        a.domain.shouldEqualToInterval(Interval(0.0, FastMath.PI / 2), 1.0e-4)
        (a.originalCurve as ConicSection).shouldEqualToConicSection(c)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthReparametrized(c, 101)
        a.evaluate(PI * 0 / 8).shouldEqualToPoint(Point.xy(0.0, 1.0), 1.0e-4)
        a.evaluate(PI * 1 / 8).shouldEqualToPoint(Point.xy(FastMath.cos(PI*3/8), FastMath.sin(PI*3/8)), 1.0e-4)
        a.evaluate(PI * 2 / 8).shouldEqualToPoint(Point.xy(R2/2, R2/2), 1.0e-4)
        a.evaluate(PI * 3 / 8).shouldEqualToPoint(Point.xy(FastMath.cos(PI*1/8), FastMath.sin(PI*1/8)), 1.0e-4)
        a.evaluate(a.domain.end).shouldEqualToPoint(Point.xy(1.0, 0.0), 1.0e-4)

        val ps = a.evaluateAll(5)
        ps.size().shouldEqualTo(5)
        ps[0].shouldEqualToPoint(Point.xy(0.0, 1.0), 1.0e-4)
        ps[1].shouldEqualToPoint(Point.xy(FastMath.cos(PI*3/8), FastMath.sin(PI*3/8)), 1.0e-4)
        ps[2].shouldEqualToPoint(Point.xy(R2/2, R2/2), 1.0e-4)
        ps[3].shouldEqualToPoint(Point.xy(FastMath.cos(PI*1/8), FastMath.sin(PI*1/8)), 1.0e-4)
        ps[4].shouldEqualToPoint(Point.xy(1.0, 0.0), 1.0e-4)
    }

    @Test
    fun testArcLength() {
        println("ArcLength")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthReparametrized(c, 101)
        a.arcLength().shouldBeCloseTo(PI/2, 1.0e-4)
    }

    @Test
    fun testToOriginalParam() {
        println("ToOriginalParam")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthReparametrized(c, 101)

        a.toOriginalParam(0.0).shouldBeCloseTo(0.0, 1.0e-3)
        a.toOriginalParam(PI/4).shouldBeCloseTo(0.5, 1.0e-3)
        a.toOriginalParam(a.domain.end).shouldBeCloseTo(1.0, 1.0e-3)
    }

    @Test
    fun testArcLengthUntil() {
        println("ArcLengthUntil")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthReparametrized(c, 101)

        a.arcLengthUntil(0.0).shouldBeCloseTo(0.0, 1.0e-4)
        a.arcLengthUntil(0.5).shouldBeCloseTo(PI/4, 1.0e-4)
        a.arcLengthUntil(1.0).shouldBeCloseTo(PI/2, 1.0e-4)
    }
}