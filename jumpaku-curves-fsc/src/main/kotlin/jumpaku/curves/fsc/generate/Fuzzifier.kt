package jumpaku.curves.fsc.generate

import jumpaku.curves.core.curve.bspline.BSpline

interface Fuzzifier {

    fun fuzzify(crisp: BSpline, ts: List<Double>): List<Double>
}