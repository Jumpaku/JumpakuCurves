package jumpaku.curves.fsc.generate.fit

import io.vavr.Tuple3
import org.apache.commons.math3.linear.DiagonalMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.util.asVavr
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2
import jumpaku.curves.core.util.component3


class BezierFitter(val degree: Int) : Fitter<Bezier> {

    init {
        require(degree >= 0) { "degree($degree) is negative" }
    }

    fun basis(i: Int, t: Double): Double = Bezier.basis(degree, i, t)

    override fun fit(data: List<WeightedParamPoint>): Bezier {
        require(data.size >= 2) { "data.size == ${data.size}, too few data" }

        val (ds, ts, ws) = data.asVavr().unzip3 { (pt, w) -> Tuple3(pt.point, pt.param, w) }

        val distinct = data.distinctBy(WeightedParamPoint::param)
        if(distinct.size <= degree){
            return BezierFitter(degree - 1).fit(data).elevate()
        }

        val d = ds.map { doubleArrayOf(it.x, it.y, it.z) }
                .toJavaArray(DoubleArray::class.java)
                .let(MatrixUtils::createRealMatrix)
        val b = ts.map { t -> (0..degree).map { basis(it, t) } }
                .map(List<Double>::toDoubleArray)
                .toJavaArray(DoubleArray::class.java)
                .let(MatrixUtils::createRealMatrix)
        val w = DiagonalMatrix(ws.toMutableList().toDoubleArray())
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b)).solver
                .solve(b.transpose().multiply(w).multiply(d))
                .let { it.data.map { Point.xyz(it[0], it[1], it[2]) } }

        return Bezier(p)
    }
}
