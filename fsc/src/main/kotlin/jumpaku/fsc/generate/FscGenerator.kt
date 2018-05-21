package jumpaku.fsc.generate

import io.vavr.collection.Array
import jumpaku.core.geom.Point
import jumpaku.core.curve.Interval
import jumpaku.core.geom.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.generate.fit.BSplineFitter
import jumpaku.fsc.generate.fit.createModelMatrix
import org.apache.commons.math3.linear.ArrayRealVector


interface Fuzzifier {
    fun fuzzify(crisp: BSpline): BSpline
}

class LinearFuzzifier(
        val velocityCoefficient: Double,
        val accelerationCoefficient: Double): Fuzzifier {
    
    val nSamples: Int = 10
    
    override fun fuzzify(crisp: BSpline): BSpline {
        val derivative1 = crisp.derivative
        val derivative2 = derivative1.derivative
        val degree = crisp.degree
        val knot = crisp.knotVector
        val n = knot.extractedKnots.size() * degree * nSamples
        val ts = crisp.domain.sample(n)
        val fs = ts.map {
            val v = derivative1(it).length()
            val a = derivative2(it).length()
            velocityCoefficient * v + accelerationCoefficient * a + 1.0
        }
        val targetVector = fs.toJavaArray(Double::class.java).run(::ArrayRealVector)
        val modelMatrix = createModelMatrix(ts, degree, knot)
        val fuzzyControlPoints = nonNegativeLinearLeastSquare(modelMatrix, targetVector).toArray()
                .zip(crisp.controlPoints, { r, (x, y, z) -> Point.xyzr(x, y, z, r) })

        return BSpline(fuzzyControlPoints, knot)
    }

}

class FscFitter(
        val degree: Int = 3,
        val knotSpan: Double = 0.1) {

    fun fit(data: Array<ParamPoint>): BSpline {
        return BSplineFitter(degree, Interval(data.head().param, data.last().param), knotSpan).fit(data)
    }
}

class FscGenerator(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val preparer: DataPreparer = DataPreparer(
                maxParamSpan = knotSpan / degree,
                innerSpan = knotSpan/2,
                outerSpan = knotSpan/2,
                degree = degree - 1),
        val fuzzifier: Fuzzifier = LinearFuzzifier(
                velocityCoefficient = 0.006,
                accelerationCoefficient = 0.004)) {



    val fitter: FscFitter = FscFitter(degree, knotSpan)

    fun generate(data: Array<ParamPoint>): BSpline {
        val prepared = preparer.prepare(data)
        val crisp = fitter.fit(prepared)
        val fuzzified = fuzzifier.fuzzify(crisp)
        val (b, e) = fuzzified.domain
        return fuzzified.restrict(Interval(b + preparer.outerSpan, e - preparer.outerSpan))
    }
}