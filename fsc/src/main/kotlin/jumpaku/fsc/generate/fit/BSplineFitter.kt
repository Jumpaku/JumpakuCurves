package jumpaku.fsc.generate.fit

import io.vavr.API.Tuple
import io.vavr.collection.Array
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.util.Precision
import jumpaku.core.geom.Point
import jumpaku.core.geom.WeightedParamPoint
import jumpaku.core.geom.transformParams
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3

fun createModelMatrix(sataParams: Array<Double>, degree: Int, knotVector: KnotVector): RealMatrix {
    val n = knotVector.extractedKnots.size() - degree - 1
    val sparse = OpenMapRealMatrix(sataParams.size(), n)
    sataParams.map { t ->
                (0..(n - 1)).map { BSpline.basis(t, it, knotVector) }
            }
            .forEachIndexed { i, row ->
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
            degree, KnotVector.clamped(domain, degree, domain.sample(delta).size() + degree*2))

    override fun fit(data: Array<WeightedParamPoint>): BSpline {
        require(data.nonEmpty()) { "empty data" }
        require(!data.isSingleValued) { "single valued too few data" }

        val distinct = data.distinctBy(WeightedParamPoint::param)
        if(distinct.size() <= degree){
            val d = transformParams(data.map { it.paramPoint }, Interval.ZERO_ONE)
                    .getOrElse { data.map { (pp, _) -> pp.copy(param = 0.5) } }
            val b = BezierFitter(degree)
                    .fit(d, distinct.map { it.weight })
            return BSpline(b.controlPoints,
                    KnotVector.clamped(Interval(distinct.head().param, distinct.last().param), degree, degree * 2 + 2))
        }

        val (d, b, w) = data.unzip3 { (pt, w) -> Tuple(pt.point, pt.param, w) }
                .map(this::createDataMatrix, this::createBasisMatrix, this::createWeightMatrix)
        val p = QRDecomposition(b.transpose().multiply(w).multiply(b)).solver
                .solve(b.transpose().multiply(w).multiply(d))
                .let { it.data.map { Point.xyz(it[0], it[1], it[2]) } }

        return BSpline(p, knotVector)
    }

    fun createBasisMatrix(sortedDataTimes: Array<Double>): RealMatrix =
            createModelMatrix(sortedDataTimes, degree, knotVector)

    fun createDataMatrix(sortedDataPoints: Array<Point>): RealMatrix = sortedDataPoints
            .map { doubleArrayOf(it.x, it.y, it.z) }
            .toJavaArray(DoubleArray::class.java)
            .run(MatrixUtils::createRealMatrix)

    fun createWeightMatrix(sortedDataWeights: Array<Double>): RealMatrix =
            DiagonalMatrix(sortedDataWeights.toMutableList().toDoubleArray())
}

