package jumpaku.fsc.identify.nquarter

import jumpaku.core.curve.Curve
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.fuzzy.Grade
import jumpaku.fsc.identify.primitive.reference.Reference
import jumpaku.fsc.identify.nquarter.reference.NQuarterCircular
import jumpaku.fsc.identify.nquarter.reference.NQuarterElliptic

class NQuarterIdentifier(val nSamples: Int = 25, val nFmps: Int = 15) {

    private fun <C: Curve> evaluate(
            reference1: Reference, reference2: Reference, reference3: Reference, fsc: ReparametrizedCurve<C>
    ): Map<NQuarterClass, Grade> {
        val (pq1, pq2, pq3) = listOf(reference1, reference2, reference3).map { fsc.isPossible(it.reparametrized, nFmps) }
        return mapOf(
                NQuarterClass.Quarter1 to pq1,
                NQuarterClass.Quarter2 to pq2,
                NQuarterClass.Quarter3 to pq3,
                NQuarterClass.General to (!pq1 and !pq2 and !pq3)
        )
    }

    fun <C : Curve> identifyCircular(fsc: ReparametrizedCurve<C>): NQuarterIdentifyResult {
        val (q1, q2, q3) = (1..3).map { NQuarterCircular().generate(it, fsc) }
        val grades = evaluate(q1, q2, q3, fsc)
        return NQuarterIdentifyResult(grades, q1, q2, q3)
    }

    fun <C : Curve> identifyElliptic(fsc: ReparametrizedCurve<C>): NQuarterIdentifyResult {
        val (q1, q2, q3) = (1..3).map { NQuarterElliptic(nSamples).generate(it, fsc) }
        val grades = evaluate(q1, q2, q3, fsc)
        return NQuarterIdentifyResult(grades, q1, q2, q3)
    }
}