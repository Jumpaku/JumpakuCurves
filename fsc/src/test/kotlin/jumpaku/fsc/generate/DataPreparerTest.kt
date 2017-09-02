package jumpaku.fsc.generate

import io.vavr.API
import org.assertj.core.api.Assertions.assertThat
import jumpaku.core.affine.Point
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.paramPointAssertThat
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.bSplineAssertThat
import jumpaku.core.fit.BSplineFitter
import org.junit.Test


class DataPreparerTest {

    @Test
    fun testPrepare() {
        println("Prepare")
        val knots = KnotVector.clampedUniform(2, 8)
        val b = BSpline(API.Array<Point>(
                Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)),
                knots)
        val data = Interval(0.5, 2.5).sample(100).map { ParamPoint(b(it), it) }
        val a = BSplineFitter(2, knots).fit(jumpaku.fsc.generate.DataPreparer(0.1, 0.5, 0.5, 2).prepare(data))
        bSplineAssertThat(a).isEqualToBSpline(b, 0.2)

        val b2 = BSpline(API.Array<Point>(
                Point.xy(1.0, 3.0), Point.xy(2.0, 0.0), Point.xy(3.0, 5.0), Point.xy(4.0, 3.0), Point.xy(5.0, 3.0)),
                KnotVector.clampedUniform(2, 8))
        val data2 = Interval(0.2, 2.8).sample(50).map { ParamPoint(b2(it), it) }
        val a2 = BSplineFitter(2, KnotVector.clampedUniform(2, 8)).fit(jumpaku.fsc.generate.DataPreparer(0.1, 0.2, 0.2, 2).prepare(data2))
        val e2 = BSpline(API.Array(
                Point.xy(1.1157219672319155, 2.7493678060976845),
                Point.xy(1.9591584061231399, 0.09817360222120309),
                Point.xy(3.010446626771964, 4.961079201399634),
                Point.xy(4.0078822134901674, 3.0246311832085775),
                Point.xy(4.953430481558565, 2.9928530991891427)),
                knots)

        bSplineAssertThat(a2).isEqualToBSpline(e2, 1.0)
    }

    @Test
    fun testFill() {
        println("Fill")
        val data = API.Array(
                ParamPoint(Point.xy(1.0, -2.0), 10.0),
                ParamPoint(Point.xy(1.5, -3.0), 15.0),
                ParamPoint(Point.xy(2.5, -5.0), 25.0))
        val a = jumpaku.fsc.generate.DataPreparer.fill(data, 2.0)

        assertThat(a.size()).isEqualTo(9)
        paramPointAssertThat(a[0]).isEqualToParamPoint(ParamPoint(Point.xy(1.0, -2.0), 10.0))
        paramPointAssertThat(a[1]).isEqualToParamPoint(ParamPoint(Point.xy(1 + 0.5 / 3.0, -2 - 1 / 3.0), 10 + 5 / 3.0))
        paramPointAssertThat(a[2]).isEqualToParamPoint(ParamPoint(Point.xy(1 + 1 / 3.0, -2 - 2 / 3.0), 10 + 10 / 3.0))
        paramPointAssertThat(a[3]).isEqualToParamPoint(ParamPoint(Point.xy(1.5, -3.0), 15.0))
        paramPointAssertThat(a[4]).isEqualToParamPoint(ParamPoint(Point.xy(1.7, -3.4), 17.0))
        paramPointAssertThat(a[5]).isEqualToParamPoint(ParamPoint(Point.xy(1.9, -3.8), 19.0))
        paramPointAssertThat(a[6]).isEqualToParamPoint(ParamPoint(Point.xy(2.1, -4.2), 21.0))
        paramPointAssertThat(a[7]).isEqualToParamPoint(ParamPoint(Point.xy(2.3, -4.6), 23.0))
        paramPointAssertThat(a[8]).isEqualToParamPoint(ParamPoint(Point.xy(2.5, -5.0), 25.0))
    }

    @Test
    fun testExtendFront() {
        println("ExtendFront")
        val knots = KnotVector.clampedUniform(2, 8)
        val b = BSpline(API.Array<Point>(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.5, 3.0).sample(100).map { ParamPoint(b(it), it) }
        val subdivided = BSplineFitter(2, knots).fit(jumpaku.fsc.generate.DataPreparer.extendFront(data, 0.5)).subdivide(2.0)
        bSplineAssertThat(subdivided._1()).isEqualToBSpline(b.subdivide(2.0)._1(), 0.2)
        bSplineAssertThat(subdivided._2()).isEqualToBSpline(b.subdivide(2.0)._2(), 0.01)
    }

    @Test
    fun testExtendBack() {
        println("ExtendBack")
        val knots = KnotVector.clampedUniform(2, 8)
        val b = BSpline(API.Array<Point>(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.0, 2.5).sample(100).map { ParamPoint(b(it), it) }
        val subdivided = BSplineFitter(2, knots).fit(jumpaku.fsc.generate.DataPreparer.extendBack(data, 0.5)).subdivide(1.0)
        bSplineAssertThat(subdivided._1()).isEqualToBSpline(b.subdivide(1.0)._1(), 0.01)
        bSplineAssertThat(subdivided._2()).isEqualToBSpline(b.subdivide(1.0)._2(), 0.2)
    }
}