package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.AffineTransform
import jumpaku.curves.core.transform.AffineTransformable
import jumpaku.curves.core.transform.SimilarityTransform
import jumpaku.curves.core.transform.SimilarityTransformable

interface Curve : (Double) -> Point, AffineTransformable<Curve>, SimilarityTransformable<Curve> {

    val domain: Interval

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    override operator fun invoke(t: Double): Point

    operator fun invoke(sampler: Sampler): List<Point> = invoke(sampler.sample(domain))

    operator fun invoke(sortedParams: List<Double>): List<Point> = sortedParams.map { invoke(it) }

    fun sample(sampler: Sampler): List<ParamPoint> = sample(sampler.sample(domain))

    fun sample(sortedParams: List<Double>): List<ParamPoint> = invoke(sortedParams).zip(sortedParams, ::ParamPoint)

    fun toCrisp(): Curve = object : Curve {
        override val domain: Interval = this@Curve.domain
        override fun invoke(t: Double): Point = this@Curve.invoke(t).toCrisp()
    }

    fun restrict(subDomain: Interval): Curve = CurveRestriction(this, subDomain)

    fun restrict(begin: Double, end: Double): Curve = restrict(Interval(begin, end))

    override fun affineTransform(a: AffineTransform): Curve = AffineTransformed(this, a)

    override fun similarityTransform(a: SimilarityTransform): Curve = SimilarityTransformed(this, a)
}
