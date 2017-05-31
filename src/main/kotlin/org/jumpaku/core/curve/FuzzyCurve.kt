package org.jumpaku.curve

import io.vavr.collection.Array
import org.jumpaku.affine.Point
import org.jumpaku.fuzzy.Grade


interface FuzzyCurve : Curve {

    fun sampleArcLength(n: Int): Array<Point>

    fun possibility(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = sampleArcLength(n)
        val otherSamples = sampleArcLength(n)
        val p = selfSamples.zipWith(otherSamples, Point::possibility)
                .reduce(Grade::and)
        val pr = selfSamples.zipWith(otherSamples.reverse(), Point::possibility)
                .reduce(Grade::and)

        return p or pr
    }

    fun necessity(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = sampleArcLength(n)
        val otherSamples = sampleArcLength(n)
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