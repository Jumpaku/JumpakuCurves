package jumpaku.curves.core.test.curve.bspline_old

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline_old.Nurbs
import jumpaku.curves.core.curve.bspline_old.NurbsJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.WeightedPoint
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class NurbsJsonTest {

    val n = Nurbs(listOf(
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
            WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
            KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))

    @Test
    fun testNurbsJson() {
        println("NurbsJson")
        val a = NurbsJson.toJsonStr(n).parseJson().let { NurbsJson.fromJson(it) }
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(n)))
    }
}