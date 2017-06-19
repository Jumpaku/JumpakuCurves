package org.jumpaku.core.fitting

import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.KnotVector
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2

fun createModelMatrix(sortedDataTimes: Array<Double>, degree: Int, knotVector: KnotVector): RealMatrix {
    val n = knotVector.size() - degree - 1
    val sparse = OpenMapRealMatrix(sortedDataTimes.size(), n)
    sortedDataTimes
            .map { t ->
                (0..(n - 1)).map { BSpline.basis(t, degree, it, knotVector) }
            }
            .forEachIndexed { i, row ->
                row.forEachIndexed { j, value ->
                    if (!Precision.equals(value, 0.0, 1.0e-10)){
                        sparse.setEntry(i, j, value)
                    }
                }
            }
    return sparse
}

class BSplineFitting(
        val degree: Int,
        val knotVector: KnotVector,
        val createWeightMatrix: (Array<ParamPoint>) -> DiagonalMatrix = {
            DiagonalMatrix(Stream.fill(it.size(), { 1.0 }).toJavaArray(Double::class.java).toDoubleArray())
        }) : Fitting<BSpline>{

    constructor(degree: Int, domain: Interval, delta: Double) : this(
            degree, KnotVector.clampedUniform(domain, degree, domain.sample(delta).size() + degree*2))

    override fun fit(data: Array<ParamPoint>): BSpline {
        val (d, b) = data.unzip { (p, t) -> Tuple(p, t) }
                .map(this::createDataMatrix, this::createBasisMatrix)
        val w = createWeightMatrix(data)
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b)).solver
                .solve(b.transpose().multiply(w).multiply(d))
                .run { this.data.map { Point.xyz(it[0], it[1], it[2]) } }

        return BSpline(p, knotVector)
    }

    fun createBasisMatrix(sortedDataTimes: Array<Double>): RealMatrix {
        return createModelMatrix(sortedDataTimes, degree, knotVector)
    }

    fun createDataMatrix(sortedDataPoints: Array<Point>): RealMatrix {
        return sortedDataPoints
                .map { doubleArrayOf(it.x, it.y, it.z) }
                .toJavaArray(DoubleArray::class.java)
                .run(MatrixUtils::createRealMatrix)
    }
}

