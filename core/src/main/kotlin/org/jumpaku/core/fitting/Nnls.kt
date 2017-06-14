package org.jumpaku.core.fitting

import io.vavr.collection.Stream
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Pair


/**
 * minimize : |a*p - d|^2
 * parameter : p = { exp(p_j) }
 * target : d = { d_i }
 * model : a.getColumn(i) dot { exp(p_j) } == sum_j a.getEntry(i, j) * exp(p_j)
 * jacobian : a * (d/dp_j { exp(p_j) }) == a * diagonal { exp(p_j) }
 * @param modelMatrix For speed, use SparseMatrix if possible.
 */
fun nonNegativeLinearLeastSquare(
        modelMatrix: RealMatrix,
        targetVector: RealVector,
        weightMatrix: DiagonalMatrix = DiagonalMatrix(Stream.fill(targetVector.dimension, { 1.0 }).toJavaArray(Double::class.java).toDoubleArray())
): RealVector {
    val problem = LeastSquaresBuilder()
            .start(ArrayRealVector(modelMatrix.columnDimension, 4.0))
            .model(NonNegativeLinearModel(modelMatrix))
            .target(targetVector)
            .lazyEvaluation(false)
            .maxEvaluations(50)
            .maxIterations(50)
            .weight(weightMatrix)
            .build()
    val result = LevenbergMarquardtOptimizer()
            .withInitialStepBoundFactor(1.0)
            .withParameterRelativeTolerance(1.0e-3)
            .withCostRelativeTolerance(targetVector.dimension * 1.0e-4)
            .optimize(problem)
    return result.point.map(FastMath::exp)
}

class NonNegativeLinearModel(val a: RealMatrix) : MultivariateJacobianFunction {

    override fun value(point: RealVector): Pair<RealVector, RealMatrix> {
        val ep = point.map(FastMath::exp)
        val value = a.operate(ep)
        val derivativeEp = DiagonalMatrix(ep.toArray())
        val jacobian = a.multiply(derivativeEp)
        return Pair(value, jacobian)
    }
}
