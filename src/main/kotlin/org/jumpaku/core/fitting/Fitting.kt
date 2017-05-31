package org.jumpaku.fitting

import io.vavr.collection.Array
import org.jumpaku.affine.Point
import org.jumpaku.curve.Curve



data class TimeSeriesPoint(
        val point: Point,
        val time: Double = System.nanoTime()*1.0e-9)

interface Fitting<out C : Curve> {

    fun fit(data: Array<TimeSeriesPoint>): C
}