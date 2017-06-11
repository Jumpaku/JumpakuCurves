package org.jumpaku.core.fitting

import io.vavr.API.*
import io.vavr.collection.Array
import org.apache.commons.math3.linear.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.Knot
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2

fun createModelMatrix(sortedDataTimes: Array<Double>, degree: Int, knotValues: Array<Double>): RealMatrix {
    return sortedDataTimes
            .map { t ->
                (0..(knotValues.size() - degree - 2)).map { BSpline.basis(t, degree, it, knotValues) }
            }
            .map(List<Double>::toDoubleArray)
            .toJavaArray(DoubleArray::class.java)
            .run(MatrixUtils::createRealMatrix)
}

class BSplineFitting(
        val degree: Int,
        val knots: Array<Knot>,
        val createWeightMatrix: (Array<TimeSeriesPoint>) -> RealMatrix = { MatrixUtils.createRealIdentityMatrix(it.size()) }
) : Fitting<BSpline>{

    constructor(degree: Int, domain: Interval, delta: Double) : this(degree, Knot.clampedUniformKnots(domain, degree, domain.sample(delta).size() + degree*2))

    private val knotValues: Array<Double> = knots.flatMap(Knot::toArray)

    override fun fit(data: Array<TimeSeriesPoint>): BSpline {
        val (d, b) = data.unzip { (p, t) -> Tuple(p, t) }
                .map(this::createDataMatrix, this::createBasisMatrix)
        val w = createWeightMatrix(data)
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b)).solver
                .solve(b.transpose().multiply(w).multiply(d))
                .run { this.data.map { Point.xyz(it[0], it[1], it[2]) } }

        return BSpline(p, knots)
    }

    fun createBasisMatrix(sortedDataTimes: Array<Double>): RealMatrix {
        return createModelMatrix(sortedDataTimes, degree, knotValues)
    }

    fun createDataMatrix(sortedDataPoints: Array<Point>): RealMatrix {
        return MatrixUtils.createRealMatrix(
                sortedDataPoints.map { doubleArrayOf(it.x, it.y, it.z) }
                        .toJavaArray(DoubleArray::class.java))
    }
}

