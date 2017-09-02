package jumpaku.core.curve.arclength

import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import jumpaku.core.affine.Point
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.curve.Interval
import jumpaku.core.curve.intervalAssertThat
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.curve.rationalbezier.conicSectionAssertThat
import org.junit.Test



class ArcLengthAdapterTest {

    val R2 = FastMath.sqrt(2.0)

    val PI = FastMath.PI

    @Test
    fun testProperties() {
        println("Properties")
        val c = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 1.0), Point.xyr(1.0, 0.0, 1.0), R2 / 2)
        val a = ArcLengthAdapter(c, 101)
        intervalAssertThat(a.domain).isEqualToInterval(Interval(0.0, FastMath.PI / 2), 1.0e-4)
        conicSectionAssertThat(a.originalCurve as ConicSection).isEqualConicSection(c)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthAdapter(c, 101)
        pointAssertThat(a.evaluate(PI * 0 / 8)).isEqualToPoint(Point.xy(0.0, 1.0), 1.0e-4)
        pointAssertThat(a.evaluate(PI * 1 / 8)).isEqualToPoint(Point.xy(FastMath.cos(PI*3/8), FastMath.sin(PI*3/8)), 1.0e-4)
        pointAssertThat(a.evaluate(PI * 2 / 8)).isEqualToPoint(Point.xy(R2/2, R2/2), 1.0e-4)
        pointAssertThat(a.evaluate(PI * 3 / 8)).isEqualToPoint(Point.xy(FastMath.cos(PI*1/8), FastMath.sin(PI*1/8)), 1.0e-4)
        pointAssertThat(a.evaluate(a.domain.end)).isEqualToPoint(Point.xy(1.0, 0.0), 1.0e-4)

        val ps = a.evaluateAll(5)
        assertThat(ps.size()).isEqualTo(5)
        pointAssertThat(ps[0]).isEqualToPoint(Point.xy(0.0, 1.0), 1.0e-4)
        pointAssertThat(ps[1]).isEqualToPoint(Point.xy(FastMath.cos(PI*3/8), FastMath.sin(PI*3/8)), 1.0e-4)
        pointAssertThat(ps[2]).isEqualToPoint(Point.xy(R2/2, R2/2), 1.0e-4)
        pointAssertThat(ps[3]).isEqualToPoint(Point.xy(FastMath.cos(PI*1/8), FastMath.sin(PI*1/8)), 1.0e-4)
        pointAssertThat(ps[4]).isEqualToPoint(Point.xy(1.0, 0.0), 1.0e-4)
    }

    @Test
    fun testArcLength() {
        println("ArcLength")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthAdapter(c, 101)
        assertThat(a.arcLength()).isEqualTo(PI/2, withPrecision(1.0e-4))
    }

    @Test
    fun testToOriginalParam() {
        println("ToOriginalParam")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthAdapter(c, 101)

        assertThat(a.toOriginalParam(0.0)).isEqualTo(0.0, withPrecision(1.0e-3))
        assertThat(a.toOriginalParam(PI/4)).isEqualTo(0.5, withPrecision(1.0e-3))
        assertThat(a.toOriginalParam(a.domain.end)).isEqualTo(1.0, withPrecision(1.0e-3))
    }

    @Test
    fun testArcLengthUntil() {
        println("ArcLengthUntil")
        val c = ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
        val a = ArcLengthAdapter(c, 101)

        assertThat(a.arcLengthUntil(0.0)).isEqualTo(0.0, withPrecision(1.0e-4))
        assertThat(a.arcLengthUntil(0.5)).isEqualTo(PI/4, withPrecision(1.0e-4))
        assertThat(a.arcLengthUntil(1.0)).isEqualTo(PI/2, withPrecision(1.0e-4))
    }

    @Test
    fun testRepeatBisection(){
        println("RepeatBisection")
        //Assert.fail("RepeatBisection")
    }
}