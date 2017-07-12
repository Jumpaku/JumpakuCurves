package org.jumpaku.fsc.identify.reference

import io.vavr.collection.Array
import org.apache.commons.math3.analysis.function.Sigmoid
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Pair
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.Vector
import org.jumpaku.core.affine.times
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.fitting.Fitting


/**
 * Fits data to EllipticConicSection with specified beginning point and end point.
 * minimize : ||b_{w, p}(t) - d| - 0|^2
 * parameter : { w(omega) = 2sigmoid(omega)-1, p = (px, py, pz) }
 * target : 0
 * model : |b_{w, p}(t) - d| = |g(t)/f(t) - d|, where
 *  g(t) = (1-t)(2t-1)p1 + 2t(1-t)(1+w(omega))p + t(1-2t)p2,
 *  f(t) = (1-t)(2t-1) + 2t(1-t)(1+w(omega)) + t(1-2t)
 * jacobian :
 *  d{model}/d{omega} = d{b_{w, p}}/d{omega} / (2 * model) = (((d{f}/d{omega} g) - (d{f}/d{omega} g)) / f^2) / (2 * model), where
 *   d{g}/d{omega} = 2t(1-t)p*d{w}/d{omega} = 2t(1-t)p*2d{sigmoid}/d{omega} = t(1-t)p*(1+w)(1-w) = t(1-t)p*(1-w^2),
 *   d{f}/d{omega} = 2t(1-t)*d{w}/d{omega} = t(1-t)*(1+w)(1-w) = t(1-t)*(1-w^2)
 *  d{model}/d{p} = (2t(1-t)(1+w)/f(t)) / (2 * model) = t(1-t)(1+w)/f(t) / model
 */
class EllipticConicSectionFitting(val t0: Double, val t1: Double, val fsc: BSpline) : Fitting<ConicSection> {
    class EllipticConicSectionModel(val data: Array<ParamPoint>, val p0:Vector, val p2:Vector) : MultivariateJacobianFunction {
        override fun value(point: RealVector): Pair<RealVector, RealMatrix> {
            val p = Vector(point.getEntry(1), point.getEntry(2), point.getEntry(3))
            val w = Sigmoid().value(point.getEntry(0))
            val d = data.map { it.point.toVector() }
            val t = data.map { it.param }

            fun g(ti: Double): Vector = (1 - ti)*(1 - 2*ti)*p0 + 2*ti*(1-ti)*(1+w)*p + ti*(2*ti - 1)*p2
            fun f(ti: Double): Double = (1 - ti)*(1 - 2*ti) + 2*ti*(1-ti)*(1+w) + ti*(2*ti - 1)

            val b = t.map { g(it)/f(it) }
            val bMinusD = b.zipWith(d, Vector::minus)
            val value = bMinusD.map(Vector::length).run { ArrayRealVector(toJavaArray(Double::class.java)) }

            val jacobian = t.zipWith(bMinusD, { ti, bMinusD_i ->
                val model_i = bMinusD_i.length()
                val g = g(ti)
                val gOmega = ti*(1 - ti)*p*(1 - w*w)
                val f = f(ti)
                val fOmega = ti*(1 - ti)*(1 - w*w)
                val db_dOmega = bMinusD_i.dot((gOmega*f - g*fOmega)/(f*f)/(2*model_i))
                val db_dp = ti*(1-ti)*(1+w)/f/model_i
                doubleArrayOf(db_dOmega, db_dp, db_dp, db_dp)
            }).run { MatrixUtils.createRealMatrix(toJavaArray(DoubleArray::class.java)) }

            return Pair(value, jacobian)
        }
    }

    override fun fit(data: Array<ParamPoint>): ConicSection {
        val initialElliptic = Circular.ofParams(t0, t1, fsc).conicSection
        val begin = initialElliptic.begin
        val end = initialElliptic.end
        val initFar = initialElliptic.far
        val initOmega = -FastMath.log(2/(initialElliptic.weight + 1) - 1)

        val problem = LeastSquaresBuilder()
                .start(ArrayRealVector(doubleArrayOf(initOmega, initFar.x, initFar.y, initFar.z)))
                .model(EllipticConicSectionModel(data, begin.toVector(), end.toVector()))
                .target(OpenMapRealVector(data.size()))
                .lazyEvaluation(false)
                .maxEvaluations(50)
                .maxIterations(50)
                .build()
        val result = LevenbergMarquardtOptimizer()
                .withInitialStepBoundFactor(1.0)
                .withParameterRelativeTolerance(1.0e-3)
                .withCostRelativeTolerance(1.0e-4)
                .optimize(problem)
        val p = result.point
        val far = Point.xyzr(p.getEntry(1), p.getEntry(2), p.getEntry(3), initFar.r)
        val w = Sigmoid().value(result.point.getEntry(0) * 2 - 1)
        return ConicSection(begin, far, end, w)
    }
}