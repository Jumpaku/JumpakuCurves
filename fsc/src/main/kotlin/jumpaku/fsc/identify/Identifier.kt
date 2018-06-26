package jumpaku.fsc.identify

import io.vavr.collection.Array
import io.vavr.collection.List
import io.vavr.collection.Stream
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.geom.line
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import java.util.*


fun reparametrize(fsc: BSpline, maxSamples: Int = 65): ReparametrizedCurve<BSpline> = fsc.run {
    val ts = knotVector.knots.map { it.value }.filter { it in domain }
    ReparametrizedCurve(fsc, if (ts.size() <= maxSamples) ts else approximateParams(maxSamples))
}

private fun BSpline.approximateParams(n: Int): Array<Double> {
    fun Curve.evaluateError(domain: Interval): Double =
            domain.sample(3).map { evaluate(it) }.let { (p0, p1, p2) ->
                line(p0, p2).map { p1.dist(it) }.getOrElse { p1.dist(p2) }
            }
    val cache = TreeMap<Double, List<Interval>>(naturalOrder())
    cache[evaluateError(domain)] = List.of(domain)
    while (cache.size < n) {
        cache.pollLastEntry().value.forEach { i ->
            val (t0, t1, t2) = i.sample(3)
            val i0 = Interval(t0, t1)
            cache.compute(evaluateError(i0)) { _, v -> v?.prepend(i0) ?: List.of(i0) }
            val i1 = Interval(t1, t2)
            cache.compute(evaluateError(i1)) { _, v -> v?.prepend(i1) ?: List.of(i1) }
        }
    }
    return Array.ofAll(cache.values.flatMap { it.map { it.begin } } + domain.end).sorted()
}

private fun repeatBisect(domain: Interval, n: Int = 2, evaluateError: (Interval)->Double): Stream<Interval> {
    val cache = TreeMap<Double, List<Interval>>(naturalOrder())
    cache[evaluateError(domain)] = List.of(domain)
    while (true) {
        cache.pollLastEntry().value.forEach { i ->
            if (cache.size >= n) return Stream.ofAll(cache.values.flatten().sortedBy { it.begin })
            val (t0, t1, t2) = i.sample(3)
            val i0 = Interval(t0, t1)
            cache.compute(evaluateError(i0)) { _, v -> v?.prepend(i0) ?: List.of(i0) }
            val i1 = Interval(t1, t2)
            cache.compute(evaluateError(i1)) { _, v -> v?.prepend(i1) ?: List.of(i1) }
        }
    }
}

interface Identifier {

    val nFmps: Int

    fun <C: Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult
}