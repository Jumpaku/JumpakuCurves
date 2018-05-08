package jumpaku.core.curve

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.affine.divide
import jumpaku.core.json.ToJson
import org.apache.commons.math3.util.FastMath


data class Interval(val begin: Double, val end: Double): ToJson, ClosedRange<Double> {

    val span: Double = end - begin

    init {
        require(begin <= end){ "begin($begin) > end($end)" }
    }

    override val start: Double = begin

    override val endInclusive: Double = end

    fun sample(samplesCount: Int): Array<Double> {
        require(samplesCount >= 2) { "samplesCount($samplesCount) < 2" }
        return Stream.range(0, samplesCount)
                .map { begin.divide(it / (samplesCount - 1.0), end).coerceIn(this) }
                .toArray()
    }

    fun sample(delta: Double): Array<Double> = sample(maxOf(1, FastMath.ceil((end - begin) / delta).toInt()) + 1)

    override operator fun contains(value: Double): Boolean = value in begin..end

    operator fun contains(i: Interval): Boolean = i.begin in begin..i.end && i.end in i.begin..end

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("begin" to begin.toJson(), "end" to end.toJson())

    companion object {

        val ZERO_ONE = Interval(0.0, 1.0)

        fun fromJson(json: JsonElement): Option<Interval> =
                Try.ofSupplier { Interval(json["begin"].double, json["end"].double) }.toOption()
    }
}
