package org.jumpaku.core.fsci.reference


import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.rationalbezier.LineSegment
import org.jumpaku.core.curve.ParamPoint

class Linear(val lineSegment: LineSegment) : Reference {

    override val fuzzyCurve: FuzzyCurve get() = object : FuzzyCurve {

        override val domain: Interval get() = Interval.ZERO_ONE

        override fun evaluate(t: Double): Point = lineSegment(t)
    }

    companion object {

        /**
         * @param t0 time parameter at front representation point
         * @param t1 time parameter at back representation point
         */
        fun create(t0: Double, t1: Double, fsc: FuzzyCurve): Linear {
            val arcLengthFsc = fsc.toArcLengthCurve()
            val l = arcLengthFsc.arcLength()
            val l0 = arcLengthFsc.arcLengthUntil(t0)
            val l1 = arcLengthFsc.arcLengthUntil(t1)
            return Linear(LineSegment(ParamPoint(fsc(t0), l0 / l), ParamPoint(fsc(t1), l1 / l)))
        }
    }
}