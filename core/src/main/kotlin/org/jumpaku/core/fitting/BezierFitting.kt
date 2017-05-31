package org.jumpaku.core.fitting

import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import java.util.Comparator


class BezierFitting(val degree: Int) : Fitting<Bezier>{

    fun basis(i: Int, t: Double): Double = Bezier.basis(degree, i, t)

    override fun fit(data: Array<TimeSeriesPoint>): Bezier {
        val m = data.size() - 1
        val sortedData = data.sorted(Comparator.comparing(TimeSeriesPoint::time)).toStream()
        val (d0, t0) = sortedData[0]
        val (dm, tm) = sortedData[m]

        val (ds, ts) = sortedData.subSequence(1, m)
                .map { (p, t) -> Tuple(p.toVector(), (t - t0)/(tm - t0)) }
                .unzip { (v, t) -> Tuple(v - d0.toVector()*basis(0, t) - dm.toVector()*basis(degree, t), t) }
        val d = MatrixUtils.createRealMatrix(
                ds.map { doubleArrayOf(it.x, it.y, it.z) }
                .toJavaArray(DoubleArray::class.java))
        val b = MatrixUtils.createRealMatrix(
                ts.map { t -> (1..(degree-1)).map { basis(it, t) } }
                .map(List<Double>::toDoubleArray)
                .toJavaArray(DoubleArray::class.java))
        val p = QRDecomposition(b.transpose().multiply(b)).solver
                .solve(b.transpose().multiply(d))
                .run { Stream(*this.data).map { Point.xyz(it[0], it[1], it[2]) } }

        return Bezier(Stream.concat(Stream(d0), p, Stream(dm)))
    }
}
