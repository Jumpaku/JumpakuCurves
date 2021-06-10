package jumpaku.curves.fsc.identify.primitive.reference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point


class Reference(val base: ConicSection, override val domain: Interval = Interval.Unit) : Curve {

    init {
        require(domain in Interval(-1.0, 2.0)) { "domain($domain) must be in [-1.0, 2.0]" }
    }

    val complement: ConicSection = base.complement()

    val reparametrized: ReparametrizedCurve<Reference> by lazy {
        val (t0, t1) = domain
        val tsFirstHalf = listOf(
            0.0,
            0.1,
            0.2,
            0.23423197731136688,
            0.3,
            0.3219386150484724,
            0.36880698353408853,
            0.39877659250419195,
            0.4,
            0.42042357458018836,
            0.43792720933528817,
            0.45502050031932484,
            0.47386580342825335,
            0.48061658402197577,
            0.4848237183167176,
            0.4880866244954987,
            0.49085818174568274,
            0.4933401081227524,
            0.49564498700004356,
            0.49784647668643145
        )

        val ts = listOf(
            Interval(t0, 0.0).sample(0.1),
            tsFirstHalf,
            listOf(0.5),
            tsFirstHalf.asReversed().map { 1.0 - it },
            Interval(1.0, t1).sample(0.1)
        ).flatten()
        ReparametrizedCurve.of(this, ts.filter { it in domain })
    }

    override fun invoke(t: Double): Point {
        require(t in domain) { "t($t) must be in domain($domain)" }
        return when (t) {
            in domain.begin..0.0 -> complement.reverse()((t + 1).coerceIn(0.0..1.0))
            in 0.0..1.0 -> base(t)
            in 1.0..domain.end -> complement.reverse()((t - 1).coerceIn(0.0..1.0))
            else -> error("")
        }
    }
}

