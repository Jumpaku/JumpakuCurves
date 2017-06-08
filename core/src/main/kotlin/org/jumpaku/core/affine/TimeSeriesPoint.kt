package org.jumpaku.core.affine

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API
import io.vavr.control.Option
import org.jumpaku.core.json.prettyGson

data class TimeSeriesPoint(
        val point: Point,
        val time: Double = System.nanoTime()*1.0e-9) : Divisible<TimeSeriesPoint> {

    override fun divide(t: Double, p: TimeSeriesPoint): TimeSeriesPoint {
        return TimeSeriesPoint(point.divide(t, p.point), time.divide(t, p.time))
    }

    override fun toString(): String = TimeSeriesPointJson.toJson(this)
}

data class TimeSeriesPointJson(
        val point: PointJson,
        val time: Double){

    fun timeSeriesPoint(): TimeSeriesPoint = TimeSeriesPoint(point.point(), time)

    companion object {

        fun toJson(p: TimeSeriesPoint): String = prettyGson.toJson(TimeSeriesPointJson(
                p.point.run { PointJson(x, y, z, r) }, p.time))

        fun fromJson(json: String): Option<TimeSeriesPoint> {
            return try {
                API.Option(prettyGson.fromJson<TimeSeriesPointJson>(json).timeSeriesPoint())
            } catch(e: Exception) {
                API.None()
            }
        }
    }
}