package jumpaku.core.curve.arclength

import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.analysis.solvers.BrentSolver
import jumpaku.core.affine.Point
import jumpaku.core.curve.Curve
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.fit.chordalParametrize


tailrec fun <C: Curve> repeatBisect(
        curve: C,
        shouldBisect: (C) -> Boolean,
        restrict: (C, Interval) -> C,
        result: Stream<Interval> = Stream.of(curve.domain)): Stream<Interval> {
    val restricted = result.map { it to restrict(curve, it) }
    return if (restricted.none { (_, c) -> shouldBisect(c) }) result
    else repeatBisect(curve, shouldBisect, restrict, restricted.flatMap { (i, c) ->
        if (shouldBisect(c)) i.sample(3).zipWithNext(::Interval)
        else Stream.of(i)
    })
}

fun approximate(curve: Curve,
                shouldBisect: (Curve) -> Boolean,
                restrict: (Curve, Interval) -> Curve): ArcLengthReparametrized {
    val subDomain = repeatBisect(curve, shouldBisect, restrict).map { it.begin }.append(curve.domain.end)
    return ArcLengthReparametrized(curve, subDomain.toArray())
}

/**
 * Approximates curve with polyline.
 */
class ArcLengthReparametrized(val originalCurve: Curve, private val originalParams: Array<Double>) : FuzzyCurve {

    constructor(curve: Curve, n: Int) : this(curve, curve.domain.sample(n))

    private val polyline: Polyline = Polyline(originalParams.map(originalCurve))

    private val arcLengthParams: Array<Double> = chordalParametrize(polyline.points).map { it.param }

    init {
        require(originalParams.size() == arcLengthParams.size()) { "originalParams.size() != arcLengthParams.size()" }
    }

    override val domain: Interval = polyline.domain

    override fun evaluate(t: Double): Point = polyline(t)

    override fun evaluateAll(n: Int): Array<Point> = polyline.evaluateAll(n)

    override fun evaluateAll(delta: Double): Array<Point> = polyline.evaluateAll(delta)

    override fun reparametrizeArcLength(): ArcLengthReparametrized = ArcLengthReparametrized(this, arcLengthParams)

    fun arcLength(): Double = domain.end

    fun toOriginalParam(arcLengthParam: Double): Double {
        require(arcLengthParam in polyline.domain) {
            "arcLengthParam($arcLengthParam) is out of domain(${polyline.domain})" }

        val relative = 1.0e-8
        val absolute = 1.0e-5
        val maxEval = 50
        return BrentSolver(relative, absolute).solve(
                maxEval,
                { arcLengthUntil(it) - arcLengthParam },
                originalCurve.domain.begin,
                originalCurve.domain.end)
    }

    fun arcLengthUntil(originalParam: Double): Double {
        require(originalParam in originalCurve.domain) {
            "originalParam($originalParam) is out of domain(${originalCurve.domain})" }

        val index = originalParams.search(originalParam)
        return when{
            index < 0 -> arcLengthParams[-index - 2] + polyline.points[-index - 2].dist(originalCurve(originalParam))
            else -> arcLengthParams[index]
        }
    }
}
