package jumpaku.curves.fsc.identify.primitive

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.identify.primitive.reference.Reference


interface Identifier  {

    val nFmps: Int

    fun isClosed(fsc: Curve): Grade = fsc.invoke(Sampler(2)).run { first().isPossible(last()) }

    fun <C : Curve> ReparametrizedCurve<C>.isPossible(reference: Reference): Grade =
            isPossible(reference.reparametrized, nFmps)

    fun <C : Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult
}

fun reparametrize(fsc: BSpline): ReparametrizedCurve<BSpline> = fsc.run {
    val nSamples = knotVector.count { it in domain } * fsc.degree * 2
    val ts = fsc.domain.sample(nSamples)
    ReparametrizedCurve.of(fsc, ts)//if (ts.size <= maxSamples) ts else approximateParams(maxSamples))
}

fun reparametrize(conicSection: ConicSection): ReparametrizedCurve<ConicSection> {
    val tsFirstHalf = listOf(
            0.0,
            0.1,
            0.2,
            0.23423197731136688,
            0.3,
            0.3219386150484724,
            0.36880698353408853,
            0.39877659250419195,
            0.4,
            0.42042357458018836,
            0.43792720933528817,
            0.45502050031932484,
            0.47386580342825335,
            0.48061658402197577,
            0.4848237183167176,
            0.4880866244954987,
            0.49085818174568274,
            0.4933401081227524,
            0.49564498700004356,
            0.49784647668643145)

    val ts = listOf(tsFirstHalf, listOf(0.5), tsFirstHalf.asReversed().map { 1.0 - it }).flatten()
    return ReparametrizedCurve.of(conicSection, ts)
}
