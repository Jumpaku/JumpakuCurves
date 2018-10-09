package jumpaku.fsc.generate

import jumpaku.core.curve.bspline.BSpline

interface Fuzzifier {
    fun fuzzify(crisp: BSpline): BSpline
}

