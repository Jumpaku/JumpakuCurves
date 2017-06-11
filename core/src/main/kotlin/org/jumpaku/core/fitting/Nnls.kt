package org.jumpaku.core.fitting

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Pair


/**
 *
 * parameter : p = { p_j }
 * target : d = { d_i }
 * model : a.getColumn(i) dot { exp(p_j) } == sum_j a.getEntry(i, j) * exp(p_j) // a*p
 * jacobian : a * (d/dp_j { exp(p_j) }) == a * diagonal { exp(p_j) }
 * minimize : sum_i (model_i - d_i)^2
 */
fun nonNegativeLinearLeastSquare(
        modelMatrix: RealMatrix,
        targetVector: RealVector,
        weightMatrix: RealMatrix = MatrixUtils.createRealIdentityMatrix(modelMatrix.rowDimension)): RealVector {
    val problem = LeastSquaresBuilder()
            .start(ArrayRealVector(modelMatrix.columnDimension, 0.0))
            .model(NonNegativeLinearModel(modelMatrix))
            .target(targetVector)
            .lazyEvaluation(false)
            .maxEvaluations(500)
            .maxIterations(50)
            .weight(weightMatrix)
            .build()
    return LevenbergMarquardtOptimizer().optimize(problem).point.map(FastMath::exp)
}

class NonNegativeLinearModel(val a: RealMatrix) : MultivariateJacobianFunction {

    override fun value(point: RealVector): Pair<RealVector, RealMatrix> {
        val ep = point.map(FastMath::exp)
        val value = a.operate(ep)
        val derivativeEp = MatrixUtils.createRealDiagonalMatrix(ep.toArray())
        val jacobian = a.multiply(derivativeEp)
        return Pair(value, jacobian)
    }
}
