package jumpaku.curves.fsc.freecurve

import io.vavr.Tuple2
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.times
import jumpaku.curves.core.util.asKt
import jumpaku.curves.core.util.asVavr
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2
import jumpaku.curves.fsc.generate.fit.BezierFitter
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.DiagonalMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition


class SmoothBezierFitter {

    val degree = 3

    private fun basis(i: Int, t: Double): Double = Bezier.basis(degree, i, t)

    private fun weight(p: ParamPoint): Double = 1 / p.point.r

    fun fitMiddle(p0: Point, v0: Vector, p1: Point, v1: Vector, data: List<ParamPoint>): Bezier {
        val ws = data.map { weight(it) }
        val (ts, ds) = data.asVavr().unzip { (p, t) ->
            val b = p0.toVector()
            val e = p1.toVector()
            val v = p.toVector()
            Tuple2(t, v - (basis(0, t) + basis(1, t)) * b - (basis(2, t) + basis(3, t)) * e)
        }
        val d = ds.flatMap { listOf(it.x, it.y, it.z) }
                .let { ArrayRealVector(it.asKt().toDoubleArray()) }
        val b = ts.flatMap { t ->
            val a1 = v0 * basis(1, t)
            val a2 = v1 * basis(2, t)
            listOf(doubleArrayOf(a1.x, a2.x), doubleArrayOf(a1.y, a2.y), doubleArrayOf(a1.z, a2.z))
        }.let { MatrixUtils.createRealMatrix(it.asKt().toTypedArray()) }

        val w = ws.flatMap { w -> List(3) { w } }
                .let { DiagonalMatrix(it.toDoubleArray()) }
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b))
                .solver.solve(b.transpose().multiply(w).operate(d))
        val cp = listOf(p0, p0 + p.getEntry(0) * v0, p1 + p.getEntry(1) * v1, p1)

        return Bezier(cp)
    }

    fun fitFront(p1: Point, v1: Vector, data: List<ParamPoint>): Bezier {
        val ws = data.map { weight(it) }
        val (ts, ps) = data.asVavr().unzip { (p, t) ->
            val e = p1.toVector()
            val d = p.toVector()
            Tuple2(t, d - (basis(2, t) + basis(3, t)) * e)
        }
        val d = ps.flatMap { listOf(it.x, it.y, it.z) }
                .let { ArrayRealVector(it.asKt().toDoubleArray()) }
        val b = ts.flatMap { t ->
            val a1 = basis(0, t)
            val a2 = basis(1, t)
            val a3 = v1 * basis(2, t)
            listOf(
                    doubleArrayOf(a1, 0.0, 0.0, a2, 0.0, 0.0, a3.x),
                    doubleArrayOf(0.0, a1, 0.0, 0.0, a2, 0.0, a3.y),
                    doubleArrayOf(0.0, 0.0, a1, 0.0, 0.0, a2, a3.z))
        }.let { MatrixUtils.createRealMatrix(it.asKt().toTypedArray()) }

        val w = ws.flatMap { w -> List(3) { w } }
                .let { DiagonalMatrix(it.toDoubleArray()) }
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b))
                .solver.solve(b.transpose().multiply(w).operate(d))
        val cp = listOf(
                Point(p.getEntry(0), p.getEntry(1), p.getEntry(2)),
                Point(p.getEntry(3), p.getEntry(4), p.getEntry(5)),
                p1 + p.getEntry(6) * v1,
                p1)

        return Bezier(cp)
    }

    fun fitBack(p0: Point, v0: Vector, data: List<ParamPoint>): Bezier =
            fitFront(p0, v0, data.map { it.copy(param = 1 - it.param) }.reversed()).reverse()

    fun fitAllFsc(data: List<ParamPoint>): Bezier = BezierFitter(degree).fit(data, data.map { weight(it) })
}