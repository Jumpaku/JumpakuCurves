package jumpaku.curves.core.curve

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.json.ToJson
import org.apache.commons.math3.util.FastMath


data class Interval(val begin: Double, val end: Double): ToJson, ClosedRange<Double> {

    init {
        require(begin.isFinite() && end.isFinite()){ "[$begin, $end]" }
        require(begin <= end){ "begin($begin) > end($end)" }
    }

    val span: Double = end - begin

    override val start: Double = begin

    override val endInclusive: Double = end

    fun sample(samplesCount: Int): List<Double> {
        require(samplesCount >= 2) { "samplesCount($samplesCount) < 2" }
        return (0 until  samplesCount)
                .map { begin.lerp(it / (samplesCount - 1.0), end).coerceIn(this) }
    }

    fun sample(delta: Double): List<Double> =
            sample(maxOf(1, FastMath.ceil((end - begin) / delta).toInt()) + 1)

    override operator fun contains(value: Double): Boolean = value in begin..end

    operator fun contains(i: Interval): Boolean = i.begin in begin..i.end && i.end in i.begin..end

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("begin" to begin.toJson(), "end" to end.toJson())

    companion object {

        val ZERO_ONE = Interval(0.0, 1.0)

        fun fromJson(json: JsonElement): Interval = Interval(json["begin"].double, json["end"].double)
    }
}
