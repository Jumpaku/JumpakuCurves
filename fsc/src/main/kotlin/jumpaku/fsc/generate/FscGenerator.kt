package jumpaku.fsc.generate

import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.generate.fit.BSplineFitter


class FscGenerator(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val preparer: DataPreparer = DataPreparer(
                maxParamSpan = knotSpan / degree,
                innerSpan = knotSpan,
                outerSpan = knotSpan,
                degree = degree - 1),
        val fuzzifier: Fuzzifier = LinearFuzzifier(
                velocityCoefficient = 0.006,
                accelerationCoefficient = 0.004)) {

    fun generate(data: List<ParamPoint>): BSpline {
        val prepared = preparer.prepare(data)
        val fitter = BSplineFitter(degree, Interval(prepared.first().param, prepared.last().param), knotSpan)
        val crisp = fitter.fit(prepared)
        val fuzzified = fuzzifier.fuzzify(crisp)
        val (b, e) = fuzzified.domain
        return fuzzified.restrict(data.first().param, data.last().param)//b + preparer.outerSpan, e - preparer.outerSpan)
    }
}