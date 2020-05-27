package jumpaku.curves.fsc.generate.fit

import io.vavr.Tuple3
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.transformParams
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.util.*
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.util.Precision

private fun createModelMatrix(dataParams: List<Double>, degree: Int, knotVector: KnotVector): RealMatrix {
    val n = knotVector.extractedKnots.size - degree - 1
    val sparse = OpenMapRealMatrix(dataParams.size, n)
    dataParams.map { t ->
        (0..(n - 1)).map { BSpline.basis(t, it, knotVector) }
    }.forEachIndexed { i, row ->
        row.forEachIndexed { j, value ->
            if (!Precision.equals(value, 0.0, 1.0e-10)) {
                sparse.setEntry(i, j, value)
            }
        }
    }
    return sparse
}

class BSplineFitter(
        val degree: Int,
        val knotVector: KnotVector) : Fitter<BSpline> {

    override fun fit(data: List<WeightedParamPoint>): BSpline {
        require(data.size >= 2) { "data.size == ${data.size}, too few data" }

        val distinct = data.distinctBy(WeightedParamPoint::param)
        if (distinct.size <= degree) {
            val d = transformParams(data.map { it.paramPoint }, range = Interval.ZERO_ONE)
            val b = BezierFitter(degree).fit(d, distinct.map { it.weight })
            val knots = KnotVector
                    .clamped(Interval(distinct.first().param, distinct.last().param), degree, degree * 2 + 2)
            return BSpline(b.controlPoints, knots)
        }

        val (d, b, w) = data.asVavr().unzip3 { (pt, w) -> Tuple3(pt.point, pt.param, w) }
                .map({ it.asKt() }, { it.asKt() }, { it.asKt() })
                .map(this::createDataMatrix, this::createBasisMatrix, this::createWeightMatrix)
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b)).solver
                .solve(b.transpose().multiply(w).multiply(d))
                .let { it.data.map { Point.xyz(it[0], it[1], it[2]) } }

        return BSpline(p, knotVector)
    }

    private fun createBasisMatrix(sortedDataTimes: List<Double>): RealMatrix =
            createModelMatrix(sortedDataTimes, degree, knotVector)

    private fun createDataMatrix(sortedDataPoints: List<Point>): RealMatrix = sortedDataPoints
            .map { doubleArrayOf(it.x, it.y, it.z) }
            .run { MatrixUtils.createRealMatrix(toTypedArray()) }

    private fun createWeightMatrix(sortedDataWeights: List<Double>): RealMatrix =
            DiagonalMatrix(sortedDataWeights.toDoubleArray())
}

