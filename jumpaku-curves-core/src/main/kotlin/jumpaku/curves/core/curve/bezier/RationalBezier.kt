package jumpaku.curves.core.curve.bezier

import jumpaku.commons.math.isOdd
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Derivative
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.*
import jumpaku.curves.core.transform.Transform


class RationalBezier(controlPoints: Iterable<Point>, weights: Iterable<Double>) : Curve, Differentiable {

    constructor(weightedControlPoints: Iterable<WeightedPoint>) :
            this(weightedControlPoints.map { it.point }, weightedControlPoints.map { it.weight })

    constructor(vararg weightedControlPoints: WeightedPoint) : this(weightedControlPoints.asIterable())

    val controlPoints: List<Point> = controlPoints.toList()

    val weights: List<Double> = weights.toList()

    init {
        require(this.controlPoints.isNotEmpty()) { "empty controlPoints" }
        require(this.weights.isNotEmpty()) { "empty weights" }
        require(this.controlPoints.size == this.weights.size) { "controlPoints.size() != weights.size()" }
    }

    val weightedControlPoints: List<WeightedPoint> = controlPoints.zip(weights, ::WeightedPoint)

    val degree: Int = weightedControlPoints.size - 1

    override val domain: Interval = Interval.ZERO_ONE

    override val derivative: Derivative by lazy {
        val ws = this@RationalBezier.weights
        val dws = ws.zipWithNext { a, b -> degree * (b - a) }
        val dp = BezierDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }).derivative

        object : Derivative {
            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }

                val wt = bezier1D(t, ws)
                val dwt = bezier1D(t, dws)
                val dpt = dp.evaluate(t)
                val rt = this@RationalBezier.evaluate(t).toVector()

                return ((dpt - dwt * rt) / wt).orThrow()
            }

            override val domain: Interval = Interval.ZERO_ONE
        }
    }

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        tailrec fun createEvaluatedPoint(t: Double, cp: List<WeightedPoint>): WeightedPoint =
                if (cp.size == 1) cp.first() else createEvaluatedPoint(t, Bezier.decasteljau(t, cp))
        return createEvaluatedPoint(t, weightedControlPoints).point
    }

    override fun toString(): String = "RationalBezier(weightedControlPoints=$weightedControlPoints)"

    fun transform(a: Transform): RationalBezier =
            RationalBezier(weightedControlPoints.map { it.copy(point = a(it.point)) })

    override fun toCrisp(): RationalBezier = RationalBezier(controlPoints.map { it.toCrisp() }, weights)

    fun restrict(i: Interval): RationalBezier = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): RationalBezier {
        require(Interval(begin, end) in domain) { "Interval($begin, $end) is out of domain($domain)" }

        return subdivide(end).first.subdivide(begin / end).second
    }

    fun reverse(): RationalBezier = RationalBezier(weightedControlPoints.reversed())

    fun elevate(): RationalBezier {
        val n = degree
        val wcp = weightedControlPoints
        val elevated = (0..(n + 1)).map {
            when (it) {
                0 -> wcp.first()
                n + 1 -> wcp.last()
                else -> wcp[it].lerp(it / (n + 1.0), wcp[it - 1])
            }
        }
        return RationalBezier(elevated)
    }

    fun reduce(): RationalBezier {
        require(degree >= 1) { "degree($degree) is too small" }
        val wcp = weightedControlPoints
        val m = wcp.size
        val n = m - 1
        val reduced = when {
            m == 2 -> listOf(wcp[0].middle(wcp[1]))
            m.isOdd() -> {
                val r = (m - 3) / 2
                val first = generateSequence(Pair(wcp.first(), 1)) { (qi, i) ->
                    Pair(wcp[i].lerp(i / (i - n).toDouble(), qi), i + 1)
                }.asIterable()
                        .take(r + 1)
                val second = generateSequence(Pair(wcp.last(), n - 2)) { (qi, i) ->
                    Pair(wcp[i + 1].lerp((i + 1 - n) / (i + 1.0), qi), i - 1)
                }.asIterable()
                        .take(r + 1)
                (first + second.reversed()).map { it.first }
            }
            else -> {
                val r = (m - 2) / 2
                val first = generateSequence(Pair(wcp.first(), 1)) { (qi, i) ->
                    Pair(wcp[i].lerp(i / (i - n).toDouble(), qi), i + 1)
                }.asIterable()
                        .take(r).map { it.first }
                val second = generateSequence(Pair(wcp.last(), n - 2)) { (qi, i) ->
                    Pair(wcp[i + 1].lerp((i + 1 - n) / (i + 1.0), qi), i - 1)
                }.asIterable()
                        .take(r).map { it.first }
                val pl = wcp[r].lerp(r / (r - n).toDouble(), first.last())
                val pr = wcp[r + 1].lerp((r + 1 - n) / (r + 1.0), second.last())
                (first + listOf(pl.middle(pr)) + second.reversed())
            }
        }

        return RationalBezier(reduced)
    }

    private fun subdivideWithoutDomain(t: Double): Pair<RationalBezier, RationalBezier> {
        var tmp = weightedControlPoints
        val first = mutableListOf(tmp.first())
        val second = mutableListOf(tmp.last())

        while (tmp.size > 1) {
            tmp = Bezier.decasteljau(t, tmp)
            first.add(tmp.first())
            second.add(0, tmp.last())
        }

        return Pair(RationalBezier(first), RationalBezier(second))
    }

    fun subdivide(t: Double): Pair<RationalBezier, RationalBezier> {
        require(t in domain) { "t($t) is out of domain($domain)" }
        return subdivideWithoutDomain(t)
    }

    fun extend(t: Double): RationalBezier {
        require(t <= domain.begin || domain.end <= t) { "t($t) is in domain($domain)" }
        return when {
            t <= domain.begin -> subdivideWithoutDomain(t).second
            t > domain.end -> subdivideWithoutDomain(t).first
            else -> error("")
        }
    }

    companion object {

        internal fun bezier1D(t: Double, weights: List<Double>): Double {
            var ws = weights
            while (ws.size > 1) {
                ws = ws.zipWithNext { w0, w1 -> w0.lerp(t, w1) }
            }
            return ws.first()
        }
    }
}

