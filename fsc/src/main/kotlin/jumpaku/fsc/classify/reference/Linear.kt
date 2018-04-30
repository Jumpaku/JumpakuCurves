package jumpaku.fsc.classify.reference


import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade

class Linear(val reference: ReferenceCurve) : Reference {

    override fun isValidFor(fsc: FuzzyCurve, nFmps: Int): Grade = reference.isPossible(fsc, nFmps)

    companion object {

        fun ofParams(t0: Double, t1: Double, fsc: FuzzyCurve): Linear {
            val reparametrized = fsc.reparametrizeArcLength()
            return Linear(referenceCurve(
                    reparametrized,
                    reparametrized.arcLengthUntil(t0),
                    reparametrized.arcLengthUntil(t1),
                    ConicSection.lineSegment(fsc(t0), fsc(t1))))
        }

        fun ofBeginEnd(fsc: FuzzyCurve): Linear = ofParams(fsc.domain.begin, fsc.domain.end, fsc)

        fun of(fsc: FuzzyCurve): Linear = ofBeginEnd(fsc)
    }
}
