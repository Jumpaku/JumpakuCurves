package org.jumpaku.core.curve

import io.vavr.collection.Array
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.arclength.ArcLengthAdapter
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.fuzzy.Grade


interface FuzzyCurve : Curve {

    fun toArcLengthCurve(): ArcLengthAdapter = ArcLengthAdapter(this, 100)

    fun possibility(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = toArcLengthCurve().evaluateAll(n)
        val otherSamples = other.toArcLengthCurve().evaluateAll(n)
        val p = selfSamples.zipWith(otherSamples, Point::possibility)
                .reduce(Grade::and)
        val pr = selfSamples.zipWith(otherSamples.reverse(), Point::possibility)
                .reduce(Grade::and)

        return p or pr
    }

    fun necessity(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = toArcLengthCurve().evaluateAll(n)
        val otherSamples = other.toArcLengthCurve().evaluateAll(n)
        val nes = selfSamples.zipWith(otherSamples, Point::necessity)
                .reduce(Grade::and)
        val nesr = selfSamples.zipWith(otherSamples.reverse(), Point::necessity)
                .reduce(Grade::and)

        return nes or nesr
    }

    companion object {
        val DEFAULT_FUZZY_MATCHING_POINTS = 30
    }
}