package org.jumpaku.fsc.identify


import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.bspline.BSpline

interface Identifier {
    fun identify(fsc: BSpline): Result
}
