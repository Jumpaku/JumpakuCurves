package org.jumpaku.core.curve.arclength

import io.vavr.collection.Array
import org.apache.commons.math3.analysis.solvers.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.divide
import org.jumpaku.core.curve.Curve
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.fitting.chordalParametrize


/**
 * Approximates curve with polyline.
 */
class ArcLengthAdapter(val originalCurve: Curve, private val polyline: Polyline) : FuzzyCurve{

    constructor(curve: Curve, n: Int) : this(curve, Polyline(curve.evaluateAll(n)))

    private val originalParams: Array<Double> = originalCurve.domain.sample(polyline.points.size())

    private val arcLengthParams: Array<Double> = chordalParametrize(polyline.points).map { it.param }

    init {
        require(originalParams.size() == arcLengthParams.size()) { "originalParams.size() != arcLengthParams.size()" }
    }

    override val domain: Interval = polyline.domain

    override fun evaluate(t: Double): Point = polyline(t)

    override fun evaluateAll(n: Int): Array<Point> = polyline.evaluateAll(n)

    override fun evaluateAll(delta: Double): Array<Point> = polyline.evaluateAll(delta)

    override fun toArcLengthCurve(): ArcLengthAdapter = this

    fun arcLength(): Double = domain.end

    fun toOriginalParam(arcLengthParam: Double): Double {
        require(arcLengthParam in polyline.domain) {
            "arcLengthParam($arcLengthParam) is out of domain(${polyline.domain})" }

        val relative = 1.0e-8
        val absolute = 1.0e-5
        val maxEval = 50
        val startValue = originalCurve.domain.begin.divide(0.5, originalCurve.domain.end)
        return BrentSolver(relative, absolute).solve(
                maxEval,
                { arcLengthUntil(it) - arcLengthParam },
                originalCurve.domain.begin,
                originalCurve.domain.end,
                startValue)
    }

    fun arcLengthUntil(originalParam: Double): Double {
        require(originalParam in originalCurve.domain) {
            "originalParam($originalParam) is out of domain(${originalCurve.domain})" }

        val index = originalParams.search(originalParam)
        return when{
            index < 0 -> arcLengthParams[-index - 1] + polyline.points[-index - 1].dist(originalCurve(originalParam))
            else -> arcLengthParams[index]
        }
    }
}
