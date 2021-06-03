package jumpaku.curves.fsc.generate.fit

import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.geom.Point
import org.apache.commons.math3.linear.DiagonalMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition


class BezierFitter(val degree: Int) : Fitter<Bezier> {

    init {
        require(degree >= 0) { "degree($degree) is negative" }
    }

    fun basis(i: Int, t: Double): Double = Bezier.basis(degree, i, t)

    override fun fit(data: List<WeightedParamPoint>): Bezier {
        require(data.size >= 2) { "data.size == ${data.size}, too few data" }

        val (ds, ts, ws) = data.map { (pt, w) -> Triple(pt.point, pt.param, w) }.run {
            val ds = map { it.first }
            val ts = map { it.second }
            val ws = map { it.third }
            Triple(ds, ts, ws)
        }

        val distinct = data.distinctBy(WeightedParamPoint::param)
        if (distinct.size <= degree) {
            return BezierFitter(degree - 1).fit(data).elevate()
        }

        val d = ds.map { doubleArrayOf(it.x, it.y, it.z) }.toTypedArray()
            .let(MatrixUtils::createRealMatrix)
        val b = ts.map { t -> (0..degree).map { basis(it, t) } }
            .map(List<Double>::toDoubleArray).toTypedArray()
            .let(MatrixUtils::createRealMatrix)
        val w = DiagonalMatrix(ws.toMutableList().toDoubleArray())
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b)).solver
            .solve(b.transpose().multiply(w).multiply(d))
            .let { it.data.map { Point.xyz(it[0], it[1], it[2]) } }

        return Bezier(p)
    }
}
