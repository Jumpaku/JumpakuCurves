package jumpaku.curves.fsc.identify.primitive.multireference.core

import jumpaku.curves.core.curve.Interval

interface MultiCurve : (Double) -> MultiPoint {

    val domain: Interval

    fun evaluate(t: Double): MultiPoint

    override operator fun invoke(t: Double): MultiPoint {
        require(t in domain) { "t($t) is out of domain($domain)" }
        return evaluate(t)
    }

    fun evaluateAll(n: Int): List<MultiPoint> = domain.sample(n).map(this::evaluate)

    fun evaluateAll(delta: Double): List<MultiPoint> = domain.sample(delta).map(this::evaluate)

    fun sample(n: Int): List<MultiParamPoint> = domain.sample(n).map { MultiParamPoint(this(it), it) }

    fun sample(delta: Double): List<MultiParamPoint> = domain.sample(delta).map { MultiParamPoint(this(it), it) }
}