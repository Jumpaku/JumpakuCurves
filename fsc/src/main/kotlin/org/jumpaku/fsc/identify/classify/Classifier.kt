package org.jumpaku.fsc.identify.classify


import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.fuzzy.Grade

interface Classifier {

    fun isClosed(fsc: BSpline): Grade = fsc.evaluateAll(2).run { head().isPossible(last()) }

    fun classify(fsc: BSpline): ClassifyResult
}
