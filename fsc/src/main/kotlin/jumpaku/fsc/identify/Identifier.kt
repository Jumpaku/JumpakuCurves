package jumpaku.fsc.identify

import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.geom.line
import jumpaku.core.util.orDefault
import java.util.*
import kotlin.collections.List
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.sorted


interface Identifier {

    val nFmps: Int

    fun <C: Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult
}

fun reparametrize(fsc: BSpline, maxSamples: Int = 65): ReparametrizedCurve<BSpline> = fsc.run {
    val ts = knotVector.knots.map { it.value }.filter { it in domain }
    ReparametrizedCurve.of(fsc, if (ts.size <= maxSamples) ts else approximateParams(maxSamples))
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
