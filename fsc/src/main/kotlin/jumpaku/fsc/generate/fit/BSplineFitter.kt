package jumpaku.fsc.generate.fit

import io.vavr.Tuple3
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.WeightedParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.transformParams
import jumpaku.core.geom.Point
import jumpaku.core.util.*
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.util.Precision

fun createModelMatrix(sataParams: List<Double>, degree: Int, knotVector: KnotVector): RealMatrix {
    val n = knotVector.extractedKnots.size - degree - 1
    val sparse = OpenMapRealMatrix(sataParams.size, n)
    sataParams.map { t ->
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

    constructor(degree: Int, domain: Interval, delta: Double) : this(
            degree, KnotVector.clamped(domain, degree, domain.sample(delta).size + degree*2))

    override fun fit(data: List<WeightedParamPoint>): BSpline {
        require(data.size >= 2) { "data.size == ${data.size}, too few data" }

        val distinct = data.distinctBy(WeightedParamPoint::param)
        if (distinct.size <= degree) {
            val d = transformParams(data.map { it.paramPoint }, Interval.ZERO_ONE).value()
                    .orDefault { data.map { (pp, _) -> pp.copy(param = 0.5) } }
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

    fun createBasisMatrix(sortedDataTimes: List<Double>): RealMatrix =
            createModelMatrix(sortedDataTimes, degree, knotVector)

    fun createDataMatrix(sortedDataPoints: List<Point>): RealMatrix = sortedDataPoints
            .map { doubleArrayOf(it.x, it.y, it.z) }
            .run { MatrixUtils.createRealMatrix(toTypedArray()) }

    fun createWeightMatrix(sortedDataWeights: List<Double>): RealMatrix =
            DiagonalMatrix(sortedDataWeights.toDoubleArray())
}

