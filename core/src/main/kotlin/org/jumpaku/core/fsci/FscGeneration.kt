package org.jumpaku.core.fsci

import io.vavr.collection.Array
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.util.*


/**
 *
 */
fun interpolate(data: Array<TimeSeriesPoint>, delta: Double): Array<TimeSeriesPoint> {
    return data.zip(data.tail()).flatMap { (a, b) ->
        val n = FastMath.ceil((a.time - b.time)/delta).toInt()
        (0..n).map { a.divide(it.toDouble()/n, b) }
    }
}

class FscGeneration {

}