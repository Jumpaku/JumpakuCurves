package jumpaku.fsc.classify.reference

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.arclength.repeatBisect
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.core.json.ToJson
import org.apache.commons.math3.util.Precision


class Reference(val base: ConicSection, override val domain: Interval = Interval.ZERO_ONE): Curve, ToJson {

    init {
        require(domain in Interval(-1.0, 2.0)) { "domain($domain) must be in [-1.0, 2.0]" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("base" to base.toJson(), "domain" to domain.toJson())

    val complement: ConicSection = base.complement().reverse()

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) must be in domain($domain)" }
        return when(t) {
            in domain.begin..0.0 -> complement((t + 1).coerceIn(0.0..1.0))
            in 0.0..1.0 -> base(t)
            in 1.0..domain.end -> complement((t - 1).coerceIn(0.0..1.0))
            else -> error("")
        }
    }

    override fun approximateParams(tolerance: Double): Array<Double> {
        val middle = base.approximateParams(1.0)
        val front = domain.copy(end = 0.0).sample(middle.size())
        val back = domain.copy(begin = 1.0).sample(middle.size())
        return Array.ofAll(front + middle + back)
    }

    override val reparameterized: ReparametrizedCurve by lazy { reparametrize(1.0) }

    companion object {

        fun fromJson(json: JsonElement): Option<Reference> = Try.ofSupplier {
            Reference(ConicSection.fromJson(json["base"]).get(), Interval.fromJson(json["domain"]).get())
        }.toOption()
    }
}
