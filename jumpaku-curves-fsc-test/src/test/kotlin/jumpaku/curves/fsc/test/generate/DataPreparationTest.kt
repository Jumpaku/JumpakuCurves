package jumpaku.curves.fsc.test.generate

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.generate.fit.weighted
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.bspline.closeTo
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.fsc.generate.fill
import jumpaku.curves.fsc.generate.fit.BSplineFitter
import jumpaku.curves.fsc.generate.prepareData
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class DataPreparationTest {

    val fillSpan = 2.0
    val extendInnerSpan = 0.5
    val extendOuterSpan = 0.5
    val extendDegree = 2

    @Test
    fun testPrepare() {
        println("Prepare")
        val knots = KnotVector.clamped(Interval(0.0, 3.0), 2, 8)
        val b = BSpline(listOf(
                Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)),
                knots)
        val data = Interval(0.5, 2.5).sample(100).map { ParamPoint(b(it), it).weighted(2.0) }
        val a = BSplineFitter(2, knots).fit(prepareData(data, 0.1, 0.5, 0.5, 2))
        assertThat(a, `is`(closeTo(b, 0.2)))

        val b2 = BSpline(listOf(
                Point.xy(1.0, 3.0), Point.xy(2.0, 0.0), Point.xy(3.0, 5.0), Point.xy(4.0, 3.0), Point.xy(5.0, 3.0)),
                KnotVector.clamped(Interval(0.0, 3.0), 2, 8))
        val data2 = Interval(0.2, 2.8).sample(50).map { ParamPoint(b2(it), it).weighted(2.0) }
        val a2 = BSplineFitter(2, KnotVector.clamped(Interval(0.0, 3.0), 2, 8)).fit(prepareData(data2, 0.1, 0.2, 0.2, 2))
        val e2 = BSpline(listOf(
                Point.xy(1.1157219672319155, 2.7493678060976845),
                Point.xy(1.9591584061231399, 0.09817360222120309),
                Point.xy(3.010446626771964, 4.961079201399634),
                Point.xy(4.0078822134901674, 3.0246311832085775),
                Point.xy(4.953430481558565, 2.9928530991891427)),
                knots)

        assertThat(a2, `is`(closeTo(e2, 1.0)))
    }

    @Test
    fun testFill() {
        println("Fill")
        val data = listOf(
                ParamPoint(Point.xy(1.0, -2.0), 10.0),
                ParamPoint(Point.xy(1.5, -3.0), 15.0),
                ParamPoint(Point.xy(2.5, -5.0), 25.0)).map { it.weighted(2.0) }
        val a = fill(data, fillSpan).map { it.paramPoint }

        assertThat(a.size, `is`(9))
        assertThat(a[0], `is`(closeTo(ParamPoint(Point.xy(1.0, -2.0), 10.0))))
        assertThat(a[1], `is`(closeTo(ParamPoint(Point.xy(1 + 0.5 / 3.0, -2 - 1 / 3.0), 10 + 5 / 3.0))))
        assertThat(a[2], `is`(closeTo(ParamPoint(Point.xy(1 + 1 / 3.0, -2 - 2 / 3.0), 10 + 10 / 3.0))))
        assertThat(a[3], `is`(closeTo(ParamPoint(Point.xy(1.5, -3.0), 15.0))))
        assertThat(a[4], `is`(closeTo(ParamPoint(Point.xy(1.7, -3.4), 17.0))))
        assertThat(a[5], `is`(closeTo(ParamPoint(Point.xy(1.9, -3.8), 19.0))))
        assertThat(a[6], `is`(closeTo(ParamPoint(Point.xy(2.1, -4.2), 21.0))))
        assertThat(a[7], `is`(closeTo(ParamPoint(Point.xy(2.3, -4.6), 23.0))))
        assertThat(a[8], `is`(closeTo(ParamPoint(Point.xy(2.5, -5.0), 25.0))))
    }

    @Test
    fun testExtendFront() {
        println("ExtendFront")
        val knots = KnotVector.clamped(Interval(0.0, 3.0), 2, 8)
        val b = BSpline(
                listOf(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.5, 3.0).sample(21).map { ParamPoint(b(it), it).weighted(2.0) }
        val (sub1, sub2) = BSplineFitter(2, knots)
                .fit(extendFront(data, extendInnerSpan, extendOuterSpan, extendDegree)).subdivide(1.0)
        assertThat(sub1.orThrow(), `is`(closeTo(b.subdivide(1.0).first.orThrow())))
        assertThat(sub2.orThrow(), `is`(closeTo(b.subdivide(1.0).second.orThrow())))
    }

    @Test
    fun testExtendBack() {
        println("ExtendBack")
        val knots = KnotVector.clamped(Interval(0.0, 3.0), 2, 8)
        val b = BSpline(
                listOf(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)), knots)
        val data = Interval(0.0, 2.5).sample(100).map { ParamPoint(b(it), it).weighted(2.0) }
        val (sub1, sub2) = BSplineFitter(2, knots)
                .fit(extendBack(data, extendInnerSpan, extendOuterSpan, extendDegree)).subdivide(1.0)
        assertThat(sub1.orThrow(), `is`(closeTo(b.subdivide(1.0).first.orThrow())))
        assertThat(sub2.orThrow(), `is`(closeTo(b.subdivide(1.0).second.orThrow())))
    }
}