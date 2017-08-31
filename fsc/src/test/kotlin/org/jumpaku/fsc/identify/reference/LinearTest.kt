package org.jumpaku.fsc.identify.reference

import com.github.salomonbrys.kotson.fromJson
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.curve.intervalAssertThat
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.curve.rationalbezier.LineSegment
import org.jumpaku.core.curve.rationalbezier.conicSectionAssertThat
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
        val cs = ConicSection(Point.xyr(2.0, 1.0, 2.0), Point.xyr(2.5, 1.5, 1.5), Point.xyr(3.0, 2.0, 1.0), 1.0)
        val l = Linear(cs, Interval(-1.0, 3.0))
        conicSectionAssertThat(l.conicSection).isEqualConicSection(cs)
        intervalAssertThat(l.domain).isEqualToInterval(Interval(-1.0, 3.0))
    }

    @Test
    fun testFuzzyCurve() {
        println("FuzzyCurve")
        val cs = ConicSection(Point.xyr(2.0, 1.0, 2.0), Point.xyr(2.5, 1.5, 1.5), Point.xyr(3.0, 2.0, 1.0), 1.0)
        val l = Linear(cs, Interval(-1.0, 3.0))
        pointAssertThat(l.reference(-1.0)).isEqualToPoint(Point.xyr(1.0, 0.0, 5.0))
        pointAssertThat(l.reference(0.0)).isEqualToPoint(Point.xyr(2.0, 1.0, 2.0))
        pointAssertThat(l.reference(1.0)).isEqualToPoint(Point.xyr(3.0, 2.0, 1.0))
        pointAssertThat(l.reference(2.0)).isEqualToPoint(Point.xyr(4.0, 3.0, 4.0))
        pointAssertThat(l.reference(3.0 )).isEqualToPoint(Point.xyr(5.0, 4.0, 7.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val cs = ConicSection(Point.xyr(2.0, 1.0, 2.0), Point.xyr(2.5, 1.5, 1.5), Point.xyr(3.0, 2.0, 1.0), 1.0)
        val l = Linear(cs, Interval(-1.0, 2.0))
        linearAssertThat(prettyGson.fromJson<LinearJson>(l.toString()).linear()).isEqualToLinear(l)
    }
}