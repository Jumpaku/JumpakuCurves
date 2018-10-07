package jumpaku.core.curve

import jumpaku.core.geom.Point
import jumpaku.core.geom.line
import jumpaku.core.util.orDefault

interface Curve : (Double)->Point {

    val domain: Interval

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    fun evaluate(t: Double): Point

    override operator fun invoke(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        return evaluate(t)
    }

    fun evaluateAll(n: Int): List<Point> = domain.sample(n).map(this::evaluate)

    fun evaluateAll(delta: Double): List<Point> = domain.sample(delta).map(this::evaluate)

    fun sample(n: Int): List<ParamPoint> = domain.sample(n).map { ParamPoint(this(it), it) }

    fun sample(delta: Double): List<ParamPoint> = domain.sample(delta).map { ParamPoint(this(it), it) }

    fun toCrisp(): Curve = object : Curve {
        override val domain: Interval = this@Curve.domain
        override fun evaluate(t: Double): Point = this@Curve.evaluate(t).toCrisp()
    }
}



fun repeatBisect(curve: Curve, tolerance: Double, domain: Interval = curve.domain): Iterable<Interval> =
        repeatBisect(curve, domain) { subDomain ->
            val (p0, p1, p2) = subDomain.sample(3).map { curve(it) }
            line(p0, p2).map { p1.dist(it) }.orDefault { p1.dist(p0) } > tolerance
        }

fun repeatBisect(curve: Curve, domain: Interval = curve.domain, shouldBisect: (Interval)->Boolean): Iterable<Interval> =
        domain.sample(3)
                .let { (t0, t1, t2) -> sequenceOf(Interval(t0, t1), Interval(t1, t2)) }.asIterable()
                .flatMap { subDomain ->
                    if (shouldBisect(subDomain)) repeatBisect(curve, subDomain, shouldBisect)
                    else  sequenceOf(subDomain).asIterable()
                }