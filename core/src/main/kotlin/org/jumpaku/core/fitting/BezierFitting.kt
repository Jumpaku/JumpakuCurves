package org.jumpaku.core.fitting

import io.vavr.API.*
import io.vavr.collection.Array
import org.apache.commons.math3.linear.DiagonalMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2


class BezierFitting(
        val degree: Int,
        val createWeightMatrix: (Array<TimeSeriesPoint>) -> DiagonalMatrix = {
            DiagonalMatrix(io.vavr.collection.Stream.fill(it.size(), { 1.0 }).toJavaArray(Double::class.java).toDoubleArray())
        }
) : Fitting<Bezier>{

    fun basis(i: Int, t: Double): Double = Bezier.basis(degree, i, t)

    override fun fit(data: Array<TimeSeriesPoint>): Bezier {
        val (ds, ts) = data.unzip { (p, t) -> Tuple(p, t) }
        val d = ds.map { doubleArrayOf(it.x, it.y, it.z) }
                .toJavaArray(DoubleArray::class.java)
                .let(MatrixUtils::createRealMatrix)
        val b = ts.map { t -> (0..degree).map { basis(it, t) } }
                .map(List<Double>::toDoubleArray)
                .toJavaArray(DoubleArray::class.java)
                .let(MatrixUtils::createRealMatrix)
        val w = createWeightMatrix(data)
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b)).solver
                .solve(b.transpose().multiply(w).multiply(d))
                .run { this.data.map { Point.xyz(it[0], it[1], it[2]) } }

        return Bezier(p)
    }
}
