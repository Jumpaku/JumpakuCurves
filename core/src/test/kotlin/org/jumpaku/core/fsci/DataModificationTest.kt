package org.jumpaku.core.fsci

import io.vavr.API
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.affine.timeSeriesDataAssertThat
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.Knot
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.bspline.bSplineAssertThat
import org.jumpaku.core.fitting.BSplineFitting
import org.junit.Test

/**
 * Created by jumpaku on 2017/06/12.
 */
class DataModificationTest {

    @Test
    fun testModify() {
        println("Modify")
        //fail("Modify not implemented.")
    }

    @Test
    fun testInterpolate() {
        println("Interpolate")
        val data = API.Array(
                TimeSeriesPoint(Point.xy(1.0, -2.0), 10.0),
                TimeSeriesPoint(Point.xy(1.5, -3.0), 15.0),
                TimeSeriesPoint(Point.xy(2.5, -5.0), 25.0))
        val a = DataModification.interpolate(data, 2.0)

        assertThat(a.size()).isEqualTo(9)
        timeSeriesDataAssertThat(a[0]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(1.0, -2.0), 10.0))
        timeSeriesDataAssertThat(a[1]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(1+0.5/3.0, -2-1/3.0), 10+5/3.0))
        timeSeriesDataAssertThat(a[2]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(1+1/3.0, -2-2/3.0), 10+10/3.0))
        timeSeriesDataAssertThat(a[3]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(1.5, -3.0), 15.0))
        timeSeriesDataAssertThat(a[4]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(1.7, -3.4), 17.0))
        timeSeriesDataAssertThat(a[5]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(1.9, -3.8), 19.0))
        timeSeriesDataAssertThat(a[6]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(2.1, -4.2), 21.0))
        timeSeriesDataAssertThat(a[7]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(2.3, -4.6), 23.0))
        timeSeriesDataAssertThat(a[8]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xy(2.5, -5.0), 25.0))
    }

    @Test
    fun testExtrapolateFront() {
        println("ExtrapolateFront")
        val knots = Knot.clampedUniformKnots(2, 8)
        val b = BSpline(API.Array<Point>(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.5, 3.0).sample(100).map { TimeSeriesPoint(b(it), it) }
        val a = BSplineFitting(2, knots).fit(DataModification.extrapolateFront(data, 0.5))
        bSplineAssertThat(a).isEqualToBSpline(b)
    }

    @Test
    fun testExtrapolateBack() {
        println("ExtrapolateBack")
        val knots = Knot.clampedUniformKnots(2, 8)
        val b = BSpline(API.Array<Point>(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.0, 2.5).sample(100).map { TimeSeriesPoint(b(it), it) }
        val a = BSplineFitting(2, knots).fit(DataModification.extrapolateBack(data, 0.5))
        bSplineAssertThat(a).isEqualToBSpline(b)
    }
}