package jumpaku.curves.fsc.identify.primitive

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.identify.primitive.reference.Reference


interface Identifier {

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
        0.03125,
        0.0625,
        0.09375,
        0.125,
        0.15625,
        0.1875,
        0.21875,
        0.25,
        0.28125,
        0.3125,
        0.34375,
        0.344891003751036,
        0.375,
        0.40625,
        0.408463201989395,
        0.435292094467113,
        0.4375,
        0.450144971866358,
        0.459613771515324,
        0.466202963249812,
        0.46875,
        0.471072560016151,
        0.474833591545413,
        0.477838553667494,
        0.480305104250766,
        0.482374944944359,
        0.484144328328536,
        0.485680957274281,
        0.487033868300738,
        0.488239477513599,
        0.489325422160593,
        0.490313082192825,
        0.491219282607574,
        0.492057471539545,
        0.492838553746554,
        0.493571492050083,
        0.49426374917309,
        0.494921617963868,
        0.495550471979488,
        0.496154958771083,
        0.496739151249323,
        0.497306668094005,
        0.497860771361831,
        0.498404447114394,
        0.498940473523741,
        0.499471479993633,
    )

    val ts = listOf(tsFirstHalf, listOf(0.5), tsFirstHalf.asReversed().map { 1.0 - it }).flatten()
    return ReparametrizedCurve.of(conicSection, ts)
}
