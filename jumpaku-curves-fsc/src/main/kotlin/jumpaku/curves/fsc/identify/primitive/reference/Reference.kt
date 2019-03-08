package jumpaku.curves.fsc.identify.primitive.reference

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point


class Reference(val base: ConicSection, override val domain: Interval = Interval.ZERO_ONE): Curve, ToJson {

    init {
        require(domain in Interval(-1.0, 2.0)) { "domain($domain) must be in [-1.0, 2.0]" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("base" to base.toJson(), "domain" to domain.toJson())

    val complement: ConicSection = base.complement()

    val reparametrized: ReparametrizedCurve<Reference> by lazy {
        val (t0, t1) = domain
        val ts = listOf(t0, -0.5, 0.0, 0.3, 0.4, 0.47, 0.49, 0.5, 0.51, 0.53, 0.6, 0.7, 1.0, 1.5, t1)
        ReparametrizedCurve.of(this, ts.filter { it in domain })
    }

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) must be in domain($domain)" }
        return when(t) {
            in domain.begin..0.0 -> complement.reverse()((t + 1).coerceIn(0.0..1.0))
            in 0.0..1.0 -> base(t)
            in 1.0..domain.end -> complement.reverse()((t - 1).coerceIn(0.0..1.0))
            else -> error("")
        }
    }

    companion object {

        fun fromJson(json: JsonElement): Reference =
                Reference(ConicSection.fromJson(json["base"]), Interval.fromJson(json["domain"]))
    }
}
