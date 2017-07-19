package org.jumpaku.fsc.identify.reference


import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.rationalbezier.LineSegment
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.rationalbezier.LineSegmentJson
import org.jumpaku.core.json.prettyGson

class Linear(val lineSegment: LineSegment) : Reference {

    override val fuzzyCurve: FuzzyCurve = object : FuzzyCurve {

        override val domain: Interval = Interval.ZERO_ONE

        override fun evaluate(t: Double): Point = lineSegment(t)
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): LinearJson = LinearJson(this)

    companion object {

        fun ofParams(t0: Double, t1: Double, fsc: BSpline): Linear {
            val arcLengthFsc = fsc.toArcLengthCurve()
            val l = arcLengthFsc.arcLength()
            val l0 = arcLengthFsc.arcLengthUntil(t0)
            val l1 = arcLengthFsc.arcLengthUntil(t1)
            return Linear(LineSegment(ParamPoint(fsc(t0), l0 / l), ParamPoint(fsc(t1), l1 / l)))
        }

        fun ofBeginEnd(fsc: BSpline): Linear {
            return ofParams(fsc.domain.begin, fsc.domain.end, fsc)
        }

        fun of(fsc: BSpline): Linear {
            return ofBeginEnd(fsc)
        }
    }
}

data class LinearJson(val lineSegment: LineSegmentJson){

    constructor(linear: Linear) : this(linear.lineSegment.json())

    fun linear(): Linear = Linear(lineSegment.lineSegment())
}