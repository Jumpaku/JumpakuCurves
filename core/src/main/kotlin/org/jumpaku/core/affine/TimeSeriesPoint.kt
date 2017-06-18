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

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): TimeSeriesPointJson = TimeSeriesPointJson(this)
}

data class TimeSeriesPointJson(
        val point: PointJson,
        val time: Double){

    constructor(timeSeriesPoint: TimeSeriesPoint) : this(timeSeriesPoint.point.json(), timeSeriesPoint.time)

    fun timeSeriesPoint(): TimeSeriesPoint = TimeSeriesPoint(point.point(), time)
}