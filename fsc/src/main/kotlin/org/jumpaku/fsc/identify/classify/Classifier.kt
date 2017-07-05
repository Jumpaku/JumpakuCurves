package org.jumpaku.fsc.identify.classify


import org.jumpaku.core.curve.bspline.BSpline

interface Classifier {

    fun classify(fsc: BSpline): Result
}
