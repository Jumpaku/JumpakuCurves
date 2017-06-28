package org.jumpaku.core.curve

import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.arclength.ArcLengthAdapter
import org.jumpaku.core.fuzzy.Grade


interface FuzzyCurve : Curve {

    fun toArcLengthCurve(): ArcLengthAdapter = ArcLengthAdapter(this, 100)

    fun isPossible(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = toArcLengthCurve().evaluateAll(n)
        val otherSamples = other.toArcLengthCurve().evaluateAll(n)
        return selfSamples.zipWith(otherSamples, Point::isPossible).reduce(Grade::and)
    }

    fun isNecessary(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = toArcLengthCurve().evaluateAll(n)
        val otherSamples = other.toArcLengthCurve().evaluateAll(n)
        return selfSamples.zipWith(otherSamples, Point::isNecessary).reduce(Grade::and)
    }

    companion object {
        val DEFAULT_FUZZY_MATCHING_POINTS = 30
    }
}