package jumpaku.fsc.test.generate

import io.vavr.collection.Array
import jumpaku.core.geom.ParamPoint
import jumpaku.core.geom.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.generate.fit.BSplineFitter
import jumpaku.core.test.affine.shouldEqualToParamPoint
import jumpaku.core.test.curve.bspline.shouldEqualToBSpline
import jumpaku.fsc.generate.DataPreparer
import org.amshove.kluent.shouldBe
import org.junit.Test

class DataPreparerTest {

    @Test
    fun testPrepare() {
        println("Prepare")
        val knots = KnotVector.clamped(Interval(0.0, 3.0), 2, 8)
        val b = BSpline(Array.of(
                Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)),
                knots)
        val data = Interval(0.5, 2.5).sample(100).map { ParamPoint(b(it), it) }
        val a = BSplineFitter(2, knots).fit(DataPreparer(0.1, 0.5, 0.5, 2).prepare(data))
        a.shouldEqualToBSpline(b, 0.2)

        val b2 = BSpline(Array.of(
                Point.xy(1.0, 3.0), Point.xy(2.0, 0.0), Point.xy(3.0, 5.0), Point.xy(4.0, 3.0), Point.xy(5.0, 3.0)),
                KnotVector.clamped(Interval(0.0, 3.0), 2, 8))
        val data2 = Interval(0.2, 2.8).sample(50).map { ParamPoint(b2(it), it) }
        val a2 = BSplineFitter(2, KnotVector.clamped(Interval(0.0, 3.0), 2, 8)).fit(DataPreparer(0.1, 0.2, 0.2, 2).prepare(data2))
        val e2 = BSpline(Array.of(
                Point.xy(1.1157219672319155, 2.7493678060976845),
                Point.xy(1.9591584061231399, 0.09817360222120309),
                Point.xy(3.010446626771964, 4.961079201399634),
                Point.xy(4.0078822134901674, 3.0246311832085775),
                Point.xy(4.953430481558565, 2.9928530991891427)),
                knots)

        a2.shouldEqualToBSpline(e2, 1.0)
    }

    @Test
    fun testFill() {
        println("Fill")
        val data = Array.of(
                ParamPoint(Point.xy(1.0, -2.0), 10.0),
                ParamPoint(Point.xy(1.5, -3.0), 15.0),
                ParamPoint(Point.xy(2.5, -5.0), 25.0))
        val a = DataPreparer.fill(data, 2.0)

        a.size().shouldBe(9)
        a[0].shouldEqualToParamPoint(ParamPoint(Point.xy(1.0, -2.0), 10.0))
        a[1].shouldEqualToParamPoint(ParamPoint(Point.xy(1 + 0.5 / 3.0, -2 - 1 / 3.0), 10 + 5 / 3.0))
        a[2].shouldEqualToParamPoint(ParamPoint(Point.xy(1 + 1 / 3.0, -2 - 2 / 3.0), 10 + 10 / 3.0))
        a[3].shouldEqualToParamPoint(ParamPoint(Point.xy(1.5, -3.0), 15.0))
        a[4].shouldEqualToParamPoint(ParamPoint(Point.xy(1.7, -3.4), 17.0))
        a[5].shouldEqualToParamPoint(ParamPoint(Point.xy(1.9, -3.8), 19.0))
        a[6].shouldEqualToParamPoint(ParamPoint(Point.xy(2.1, -4.2), 21.0))
        a[7].shouldEqualToParamPoint(ParamPoint(Point.xy(2.3, -4.6), 23.0))
        a[8].shouldEqualToParamPoint(ParamPoint(Point.xy(2.5, -5.0), 25.0))
    }

    @Test
    fun testExtendFront() {
        println("ExtendFront")
        val knots = KnotVector.clamped(Interval(0.0, 3.0), 2, 8)
        val b = BSpline(Array.of(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.5, 3.0).sample(100).map { ParamPoint(b(it), it) }
        val subdivided = BSplineFitter(2, knots).fit(DataPreparer.extendFront(data, 0.5)).subdivide(2.0)
        subdivided._1().get().shouldEqualToBSpline(b.subdivide(2.0)._1().get(), 0.2)
        subdivided._2().get().shouldEqualToBSpline(b.subdivide(2.0)._2().get(), 0.01)
    }

    @Test
    fun testExtendBack() {
        println("ExtendBack")
        val knots = KnotVector.clamped(Interval(0.0, 3.0), 2, 8)
        val b = BSpline(Array.of(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.0, 2.5).sample(100).map { ParamPoint(b(it), it) }
        val subdivided = BSplineFitter(2, knots).fit(DataPreparer.extendBack(data, 0.5)).subdivide(1.0)
        subdivided._1().get().shouldEqualToBSpline(b.subdivide(1.0)._1().get(), 0.01)
        subdivided._2().get().shouldEqualToBSpline(b.subdivide(1.0)._2().get(), 0.2)
    }
}