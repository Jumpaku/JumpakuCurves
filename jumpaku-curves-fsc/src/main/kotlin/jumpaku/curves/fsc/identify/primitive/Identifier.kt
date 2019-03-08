package jumpaku.curves.fsc.identify.primitive

import jumpaku.commons.control.orDefault
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.line
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.set


interface Identifier {

    val nFmps: Int

    fun <C: Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult
}

fun reparametrize(fsc: BSpline): ReparametrizedCurve<BSpline> = fsc.run {
    val nSamples = knotVector.knots.count { it.value in domain }*fsc.degree*2
    val ts = fsc.domain.sample(nSamples)
    ReparametrizedCurve.of(fsc, ts)//if (ts.size <= maxSamples) ts else approximateParams(maxSamples))
}

fun reparametrize(conicSection: ConicSection): ReparametrizedCurve<ConicSection>  {
    val a = listOf(
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

    val ts = listOf(listOf(0.0), a, listOf(0.5), a.asReversed().map { 1.0 - it }, listOf(1.0)).flatten()
    return ReparametrizedCurve.of(conicSection, ts)
}

private fun BSpline.approximateParams(n: Int): List<Double> {
    fun Curve.evaluateError(domain: Interval): Double =
            domain.sample(3).map { evaluate(it) }.let { (p0, p1, p2) ->
                line(p0, p2).tryMap { p1.dist(it) }.value().orDefault { p1.dist(p2) }
            }
    val cache = TreeMap<Double, List<Interval>>(naturalOrder())
    cache[evaluateError(domain)] = listOf(domain)
    while (cache.size < n) {
        cache.pollLastEntry().value.forEach { i ->
            val (t0, t1, t2) = i.sample(3)
            val i0 = Interval(t0, t1)
            cache.compute(evaluateError(i0)) { _, v -> v?.let { listOf(i0) + it } ?: listOf(i0) }
            val i1 = Interval(t1, t2)
            cache.compute(evaluateError(i1)) { _, v -> v?.let { listOf(i1) + it } ?: listOf(i1) }
        }
    }
    return (cache.values.flatMap { it.map { it.begin } } + domain.end).sorted()
}
