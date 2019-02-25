package jumpaku.curves.fsc.freecurve

import io.vavr.Tuple2
import io.vavr.Tuple4
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.util.*
import org.apache.commons.math3.analysis.solvers.BrentSolver


class SmoothResult(val conicSections: List<ConicSection>, val cubicBeziers: List<Bezier>)

class Smoother(
        val pruningFactor: Double = 1.0,
        val nFitSamples: Int = 17,
        val fscSampleSpan: Double = 0.02){

    fun smooth(fsc: BSpline, ts: List<Double>, segmentResult: SegmentResult, isClosed: Boolean = isClosed(fsc)): SmoothResult {
        val pis = segmentResult.segmentParamIndices
        val segments = segmentResult.segments

        val qs = segments.map { it.conicSection }
        val cis = fragmentCs(qs, pis.map { fsc(ts[it]) }, isClosed)
        val ks = remainConicSectionIndices(cis)
        val rs = remainCs(qs, cis, ks)

        val fis = fragmentFsc(fsc, ts, pis, isClosed)
        val gis = combineFscInterval(fsc, fis, ks)
        val bs = fitBezier(fsc, gis, rs, isClosed)

        return SmoothResult(rs, bs)
    }

    fun fragmentCs(qs: List<ConicSection>, cs: List<Point>, isClosed: Boolean): List<Option<Interval>>{
        val begins = qs.zip(cs) { q, c ->
            Interval.ZERO_ONE.sample(nFitSamples)
                    .find { q(it).dist(c) > c.r * pruningFactor }
                    ?.let { BrentSolver().solve(50, { q(it).dist(c) - c.r * pruningFactor }, 0.0, it) } ?: 1.0
        }.let {
            if (isClosed) it else it.asVavr().update(0, 0.0)
        }

        val ends = qs.zip(cs.drop(1)) { q, c ->
            Interval.ZERO_ONE.sample(nFitSamples)
                    .findLast { q(it).dist(c) > c.r * pruningFactor }
                    ?.let { BrentSolver().solve(50, { q(it).dist(c) - c.r * pruningFactor }, it, 1.0) } ?: 0.0
        }.let {
            if (isClosed) it else it.asVavr().update(it.lastIndex, 1.0)
        }

        return begins.zip(ends) { b, e -> optionWhen(b < e) { Interval(b, e) } }
    }

    fun fragmentFsc(fsc: BSpline, ts: List<Double>, pis: List<Int>, isClosed: Boolean): List<Interval>{
        val (b, e) = fsc.domain
        fun fragmentFscInterval(t: Double): Interval {
            val c = fsc(t)

            val samples0 = Interval(b, t).sample(fscSampleSpan)
            val begin = samples0
                    .findLast { fsc(it).dist(c) > c.r*pruningFactor }
                    ?.let { BrentSolver().solve(50, { fsc(it).dist(c) - c.r * pruningFactor }, it, t) } ?: b

            val samples1 = Interval(t, e).sample(fscSampleSpan)
            val end = samples1
                    .find { fsc(it).dist(c) > c.r*pruningFactor }
                    ?.let { BrentSolver().solve(50, { fsc(it).dist(c) - c.r * pruningFactor }, t, it) } ?: e

            return Interval(begin, end)
        }
        return pis.map { i ->
            when {
                !isClosed && i == pis.first() -> Interval(b, b)
                !isClosed && i == pis.last() -> Interval(e, e)
                else -> fragmentFscInterval(ts[i])
            }
        }
    }

    fun remainConicSectionIndices(cis: List<Option<Interval>>): List<Int> = cis.withIndex().flatMap { (i, opt) -> opt.map { i } }

    fun remainCs(qs: List<ConicSection>, cis: List<Option<Interval>>, ks: List<Int>): List<ConicSection> =
            ks.flatMap { cis[it].map { i -> qs[it].restrict(i) } }

    fun combineFscInterval(fsc: BSpline, fis: List<Interval>, ks: List<Int>): List<Interval> {
        if(ks.isEmpty()) return listOf(fsc.domain)

        val middles = ks.zipWithNext { prev, next -> Interval(fis[prev + 1].begin, fis[next].end) }

        val (b, e) = fsc.domain
        val front = fis[ks.first()].copy(begin = b)
        val back = fis[ks.last() + 1].copy(end = e)
        return listOf(front) + middles + listOf(back)
    }

    fun fitBezier(fsc: BSpline, gis: List<Interval>, rs: List<ConicSection>, isClosed: Boolean): List<Bezier> {
        val fitter = SmoothBezierFitter()

        if (rs.isEmpty()){
            val s = if (isClosed) fsc.close() else fsc
            return listOf(fitter.fitAllFsc(parametrize(s.evaluateAll(fscSampleSpan))))
        }
        val p0v0s = rs.map { Tuple2(it(1.0), it.differentiate(1.0)) }.asVavr().init()
        val p1v1s = rs.map { Tuple2(it(0.0), it.differentiate(0.0)) }.asVavr().tail()
        val p0v0p1v1s = p0v0s.zipWith(p1v1s) { pv0, pv1 -> Tuple4(pv0._1, pv0._2, pv1._1, pv1._2) }
        val middles = gis.asVavr().slice(1, gis.lastIndex).zip(p0v0p1v1s) { i, p0v0p1v1 ->
            val data = parametrize(fsc.restrict(i).evaluateAll(nFitSamples))
            fitter.fitMiddle(p0v0p1v1._1, p0v0p1v1._2, p0v0p1v1._3, p0v0p1v1._4, data)
        }


        val (p1, v1) = rs.first().let { Pair(it(0.0), it.differentiate(0.0)) }
        val frontPoints = gis.first().run {
            if (begin == end) List(nFitSamples) { fsc(begin) }
            else fsc.restrict(this).evaluateAll(nFitSamples)
        }

        val (p0, v0) = rs.last().let { Tuple2(it(1.0), it.differentiate(1.0)) }
        val backPoints = gis.last().run {
            if (begin == end) List(nFitSamples) { fsc(end) }
            else fsc.restrict(this).evaluateAll(nFitSamples)
        }

        return if (isClosed) {
            val data = parametrize(backPoints + frontPoints.reversed())
            middles + fitter.fitMiddle(p0, v0, p1, v1, data)
        }
        else {
            val frontData = parametrize(frontPoints)
            val backData = parametrize(backPoints)
            listOf(fitter.fitFront(p1, v1, frontData)) + middles + listOf(fitter.fitBack(p0, v0, backData))
        }
    }

    fun isClosed(s: BSpline): Boolean = s.evaluateAll(2).let { (b, e) -> e.isPossible(b).value > 0.5 }

    fun parametrize(points: List<Point>): List<ParamPoint> =
            chordalParametrize(points, range = Interval.ZERO_ONE)
                    .tryRecover { uniformParametrize(points, range = Interval.ZERO_ONE) }.value().orThrow()
}
