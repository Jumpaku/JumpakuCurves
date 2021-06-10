package jumpaku.curves.core.curve.polyline

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.geom.Point


class LineSegment(begin: ParamPoint, end: ParamPoint) : Curve by Polyline(listOf(begin, end)) {

    constructor(begin: Point, end: Point, domain: Interval = Interval.Unit) :
            this(ParamPoint(begin, domain.begin), ParamPoint(end, domain.end))

    init {
        require(begin.param < end.param) { "must be begin.param(${begin.param}) < end.param(${end.param})" }
    }

    val begin: Point = begin.point

    val end: Point = end.point

    override fun toString(): String = "LineSegment(begin=$begin, end=$end)"
}

