package jumpaku.curves.core.curve.bezier


import jumpaku.commons.control.Option
import jumpaku.commons.control.optionWhen
import jumpaku.commons.control.orDefault
import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Derivative
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.*
import jumpaku.curves.core.transform.AffineTransform
import jumpaku.curves.core.transform.SimilarityTransform
import org.apache.commons.math3.util.FastMath


/**
 * Conic section defined by 3 representation points.
 */
class ConicSection(val begin: Point, val far: Point, val end: Point, val weight: Double) : Curve, Differentiable {

    val representPoints: List<Point> get() = listOf(begin, far, end)

    val degree = 2

    override val domain: Interval = Interval.Unit

    override fun differentiate(): Derivative = object : Derivative {
        override val domain: Interval = this@ConicSection.domain
        override fun invoke(t: Double): Vector {
            require(t in domain) { "t($t) is out of domain($domain)" }

            val g =
                (1 - t) * (1 - 2 * t) * begin.toVector() + 2 * t * (1 - t) * (1 + weight) * far.toVector() + t * (2 * t - 1) * end.toVector()
            val dg_dt =
                (4 * t - 3) * begin.toVector() + 2 * (1 - 2 * t) * (1 + weight) * far.toVector() + (4 * t - 1) * end.toVector()
            val f = RationalBezier.bezier1D(t, listOf(1.0, weight, 1.0))
            val df_dt = 2 * (weight - 1) * (1 - 2 * t)

            return ((dg_dt * f - g * df_dt) / (f * f)).orThrow()
        }
    }

    fun toCrispQuadratic(): Option<RationalBezier> = optionWhen(1.0.tryDiv(weight).isSuccess) {
        RationalBezier(
            listOf(
                begin.toCrisp(),
                far.lerp(-1 / weight, begin.middle(end)).toCrisp(),
                end.toCrisp()
            ).zip(listOf(1.0, weight, 1.0), ::WeightedPoint)
        )
    }

    override fun invoke(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val wt = RationalBezier.bezier1D(t, listOf(1.0, weight, 1.0))
        return far.lerp((1 - t) * (1 - 2 * t) / wt to begin, t * (2 * t - 1) / wt to end)
    }

    override fun affineTransform(a: AffineTransform): ConicSection = ConicSection(a(begin), a(far), a(end), weight)

    override fun similarityTransform(a: SimilarityTransform): ConicSection =
        ConicSection(a(begin), a(far), a(end), weight)

    override fun toString(): String = "ConicSection(begin=$begin, far=$far, end=$end, weight=$weight)"

    override fun toCrisp(): ConicSection = ConicSection(begin.toCrisp(), far.toCrisp(), end.toCrisp(), weight)

    fun reverse(): ConicSection = ConicSection(end, far, begin, weight)

    fun complement(): ConicSection {
        val farComplement = 1.0.tryDiv(1 - weight)
            .tryMap { far.lerp(it to begin, it to end) }
            .tryRecover { far }.orThrow()
        return ConicSection(begin, farComplement, end, -weight)
    }

    fun center(): Option<Point> = 0.5.tryDiv(1 - weight).tryMap { far.lerp(it to begin, it to end) }.value()

    /**
     * Subdivides this at t into 2 conic sections
     */
    fun subdivide(t: Double): Pair<ConicSection, ConicSection> {
        val w = weight
        val p0 = begin.toVector()
        val p1 = far.toVector()
        val p2 = end.toVector()
        val m = begin.middle(end)
        val rootwt = FastMath.sqrt(RationalBezier.bezier1D(t, listOf(1.0, w, 1.0)))

        val begin0 = begin
        val end0 = invoke(t)
        val weight0 = (1 - t + t * w) / rootwt
        val far0P =
            ((begin0.toVector() + end0.toVector()) * rootwt * 0.5 + (1 - t) * p0 + t * ((1 + w) * p1 - m.toVector())) / (rootwt + 1 - t + t * w)
        val far0R =
            FastMath.abs(0.5 * (2 - 3 * t + rootwt * (2 * t * t - 3 * t + 2)) / (rootwt + 1 - t + t * w)) * begin.r +
                    FastMath.abs((t * (1 + w) * (1 + (1 - t) / rootwt)) / (rootwt + 1 - t + t * w)) * far.r +
                    FastMath.abs(0.5 * (-t + t * (2 * t - 1) / rootwt) / (rootwt + 1 - t + t * w)) * end.r
        val far0 = Point(far0P.orThrow(), far0R)

        val begin1 = end0
        val end1 = end
        val weight1 = ((1 - t) * w + t) / rootwt
        val far1P =
            ((begin1.toVector() + end1.toVector()) * rootwt * 0.5 + (1 - t) * ((1 + w) * p1 - m.toVector()) + t * p2) / (rootwt + (1 - t) * w + t)
        val far1R =
            FastMath.abs(0.5 * (3 * t - 1 + rootwt * (2 * t * t - t + 1)) / (rootwt + (1 - t) * w + t)) * begin.r +
                    FastMath.abs(((1 - t) * (1 + w) * (1 + t / rootwt)) / (rootwt + (1 - t) * w + t)) * far.r +
                    FastMath.abs(0.5 * ((1 - t) * ((1 - 2 * t) / rootwt - 1)) / (rootwt + (1 - t) * w + t)) * end.r
        val far1 = Point(far1P.orThrow(), far1R)

        return Pair(ConicSection(begin0, far0, end0, weight0), ConicSection(begin1, far1, end1, weight1))
    }

    fun clipout(interval: Interval): ConicSection = clipout(interval.begin, interval.end)

    fun clipout(begin: Double, end: Double): ConicSection {
        require(begin <= end && begin in domain && end in domain) { "begin <= end && begin in domain && end in domain" }
        return begin.tryDiv(end).tryMap { t ->
            val a = FastMath.sqrt(RationalBezier.bezier1D(end, listOf(1.0, weight, 1.0)))
            subdivide(end).first.subdivide(a * t / (t * (a - 1) + 1)).second
        }.tryRecover {
            ConicSection(this.begin, this.begin, this.begin, 1.0)
        }.orThrow()
    }

    companion object {

        /**
         * Calculates a weight as (l^2 - h^2)/(l^2 + h^2),
         * where h is distance between far and line(begin, end), l = |begin - end|/2.
         *  an elliptic arc with this weight is a sheared circular arc which has the same weight.
         */
        fun shearedCircularArc(begin: Point, far: Point, end: Point): ConicSection {
            val hh = line(begin, end).tryMap { far.distSquare(it) }.value().orDefault { begin.distSquare(far) }
            val ll = (begin - end).square() / 4
            return ConicSection(begin, far, end, ((ll - hh) / (ll + hh)).coerceIn(-0.999, 0.999))
        }

        fun lineSegment(begin: Point, end: Point): ConicSection = ConicSection(begin, begin.middle(end), end, 1.0)

    }
}

