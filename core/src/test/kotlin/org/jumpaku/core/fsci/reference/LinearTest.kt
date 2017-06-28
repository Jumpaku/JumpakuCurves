package org.jumpaku.core.fsci.reference

import com.github.salomonbrys.kotson.fromJson
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.curve.rationalbezier.LineSegment
import org.jumpaku.core.curve.rationalbezier.lineSegmentAssertThat
import org.jumpaku.core.fuzzy.Grade
import org.jumpaku.core.json.prettyGson
import org.junit.Test
import java.io.FileReader
import java.nio.file.Paths


class LinearTest {

    @Test
    fun testProperties() {
        println("Properties")
        val ls = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
        val l = Linear(ls)
        lineSegmentAssertThat(l.lineSegment).isEqualLineSegment(ls)
    }

    @Test
    fun testFuzzyCurve() {
        println("FuzzyCurve")
        val ls = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
        val l = Linear(ls)
        pointAssertThat(l.fuzzyCurve(0.0 )).isEqualToPoint(Point.xyr(1.0, 0.0, 5.0))
        pointAssertThat(l.fuzzyCurve(0.25)).isEqualToPoint(Point.xyr(2.0, 1.0, 2.0))
        pointAssertThat(l.fuzzyCurve(0.5 )).isEqualToPoint(Point.xyr(3.0, 2.0, 1.0))
        pointAssertThat(l.fuzzyCurve(0.75)).isEqualToPoint(Point.xyr(4.0, 3.0, 4.0))
        pointAssertThat(l.fuzzyCurve(1.0 )).isEqualToPoint(Point.xyr(5.0, 4.0, 7.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val ls = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
        val l = Linear(ls)
        linearAssertThat(prettyGson.fromJson<LinearJson>(l.toString()).linear()).isEqualToLinear(l)
    }

    @Test
    fun testIsValidFor() {
        println("IsValidFor")
        val path = Paths.get("./src/test/resources/org/jumpaku/core/fsci/reference/")
        for (i in 0..9){
            val fsc = FileReader(path.resolve("Fsc$i.json").toFile()).use { prettyGson.fromJson<BSplineJson>(it).bSpline() }
            val arcLength = fsc.toArcLengthCurve()
            val t0 = arcLength.toOriginalParam(arcLength.arcLength()/5)
            val t1 = arcLength.toOriginalParam(arcLength.arcLength()*3/5)
            val la = Linear.create(t0, t1, fsc)
            val le = FileReader(path.resolve("Linear$i.json").toFile()).use { prettyGson.fromJson<LinearJson>(it).linear() }
            linearAssertThat(la).isEqualToLinear(le, 10.0)

            val lp = la.isValidFor(fsc)
            val lpe = FileReader(path.resolve("LinearGrade$i.json").toFile()).use { prettyGson.fromJson<Grade>(it).value }
            assertThat(lp.value).isEqualTo(lpe)
        }
    }
}