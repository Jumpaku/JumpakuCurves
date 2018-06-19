package jumpaku.core.curve.arclength

import io.vavr.collection.Array
import io.vavr.collection.List
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.geom.middle
import jumpaku.core.util.divOption
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.util.FastMath
import kotlin.math.max
import kotlin.math.min

class Reparametrizer(curve: Curve, val originalParams: Array<Double>) {

    data class MonotonicQuadratic(val a: Double, val b: Double, val c: Double, val domain: Interval): (Double)->Double {

        val range: Interval

        init {
            require((-b).divOption(2*a).filter { it !in domain }.isDefined) { "not monotonic" }
            range = domain.let { (t0, t2) -> Interval(a*t0*t0 + b*t0 + c, a*t2*t2 + b*t2 + c) }
        }

        override fun invoke(t: Double): Double {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return (a*t*t + b*t + c).coerceIn(range)
        }

        fun invert(s: Double): Double {
            require(s in range) { "s($s) is out of range($range)" }

            val (t0, t2) = domain
            val t1 = t0.middle(t2)
            val r = FastMath.sqrt(b*b - 4*a*(c - s))
            val alpha = (-b+r).divOption(2*a)
            val beta = (-b-r).divOption(2*a)
            val axis = (-b).divOption(2*a)
            return axis.map { if (it < t1) max(alpha.get(), beta.get()) else min(alpha.get(), beta.get()) }
                    .orElse { (s - c).divOption(b) }
                    .getOrElse { t0.middle(t2) }
                    .coerceIn(domain)
        }
    }

    private val arcLengthParams: Array<Double>

    private val quadratics: Array<MonotonicQuadratic>

    init {
        require(originalParams.size() > 1) { "originalToArcLength.size() is too small"}

        val qs = originalParams.zipWithNext { t0, t2 -> interpolate(curve, t0, t2) }.let { Array.ofAll(it) }

        arcLengthParams = originalParams.foldIndexed(List.empty<Double>()) { i, ss, t ->
            if (i == 0) List.of(0.0)
            else ss.prepend(ss.head() + qs[i - 1](t))
        }.reverse().toArray()

        quadratics = qs.mapIndexed { i, q -> q.copy(c = q.c + arcLengthParams[i]) }.let { Array.ofAll(it) }

    }

    val domain: Interval = Interval(originalParams.head(), originalParams.last())

    val range: Interval = Interval(arcLengthParams.head(), arcLengthParams.last())

    fun toOriginal(arcLengthParam: Double): Double {
        require(arcLengthParam in range) { "arcLengthParam($arcLengthParam) is out of range($range)"}
        val i = arcLengthParams.search(arcLengthParam).let { if (it < 0) -it-1 else it }
        return if (i == 0) originalParams[0]
        else quadratics[i-1].let { it.invert(arcLengthParam.coerceIn(it.range)).coerceIn(domain) }
    }

    fun toArcLength(originalParam: Double): Double {
        require(originalParam in domain) { "originalParam($originalParam) is out of domain($domain)"}
        val i = originalParams.search(originalParam).let { if (it < 0) -it-1 else it }
        return if (i == 0) 0.0
        else quadratics[i-1].let { it(originalParam.coerceIn(it.domain)).coerceIn(range) }
    }

    companion object {

        private fun interpolate(curve: Curve, t0: Double, t2: Double): MonotonicQuadratic {
            val t1 = t0.middle(t2)
            val p0 = curve(t0)
            val p1 = curve(t1)
            val p2 = curve(t2)
            val s0 = 0.0
            val s1 = p0.dist(p1)
            val s2 = s1 + p1.dist(p2)
            val t = MatrixUtils.createRealMatrix(arrayOf(
                    doubleArrayOf(t0*t0, t0, 1.0),
                    doubleArrayOf(t1*t1, t1, 1.0),
                    doubleArrayOf(t2*t2, t2, 1.0)
            ))
            val t1Min = (t1 - t0)*(t1 - t0)*(s2 - s0)/((t2 - t0)*(t2 - t0)) + s0
            val t1Max = (t1 - t2)*(t1 - t2)*(s0 - s2)/((t0 - t2)*(t0 - t2)) + s2
            val s = MatrixUtils.createRealVector(doubleArrayOf(s0, s1.coerceIn(t1Min, t1Max), s2))
            val abc = QRDecomposition(t).solver.solve(s)
            return MonotonicQuadratic(abc.getEntry(0), abc.getEntry(1), abc.getEntry(2), Interval(t0, t2))
        }
    }
}