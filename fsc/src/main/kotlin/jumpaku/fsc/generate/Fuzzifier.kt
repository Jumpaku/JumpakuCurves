package jumpaku.fsc.generate

import jumpaku.core.curve.bspline.BSpline
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.DiagonalMatrix
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Pair

interface Fuzzifier {

    fun fuzzify(crisp: BSpline, ts: List<Double>): List<Double>
}