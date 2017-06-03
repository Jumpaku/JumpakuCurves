package org.jumpaku.core.fitting

import io.vavr.collection.Array
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.curve.Curve


interface Fitting<out C : Curve> {

    fun fit(data: Array<TimeSeriesPoint>): C
}