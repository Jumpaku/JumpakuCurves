package org.jumpaku.core.fitting

import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.Knot
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import java.util.Comparator


class BSplineFitting(val degree: Int, val knots: Array<Knot>) : Fitting<BSpline>{

    fun basis(knotValues: Array<Double>, i: Int, t: Double): Double = BSpline.basis(t, degree, i, knotValues)

    override fun fit(data: Array<TimeSeriesPoint>): BSpline {
        val m = data.size() - 1
        val us = knots.flatMap(Knot::toArray)
        val sortedData = data.sorted(Comparator.comparing(TimeSeriesPoint::time)).toStream()
        val (d0, _) = sortedData[0]
        val (dm, _) = sortedData[m]

        val (ds, ts) = sortedData.subSequence(1, m)
                .unzip { (v, t) -> Tuple(
                        v.toVector() - d0.toVector() * basis(us, 0, t) - dm.toVector() * basis(us, (us.size() - degree - 1) - 1, t), t) }
        val d = MatrixUtils.createRealMatrix(
                ds.map { doubleArrayOf(it.x, it.y, it.z) }
                        .toJavaArray(DoubleArray::class.java))
        val b = MatrixUtils.createRealMatrix(
                ts.map { t -> (1..((us.size() - degree - 1) - 2)).map { basis(us, it, t) } }
                        .map(List<Double>::toDoubleArray)
                        .toJavaArray(DoubleArray::class.java))
        val p = QRDecomposition(b.transpose().multiply(b)).solver
                .solve(b.transpose().multiply(d))
                .run { Stream(*this.data).map { Point.xyz(it[0], it[1], it[2]) } }

        return BSpline(Stream.concat(Stream(d0), p, Stream(dm)), knots)
    }
}