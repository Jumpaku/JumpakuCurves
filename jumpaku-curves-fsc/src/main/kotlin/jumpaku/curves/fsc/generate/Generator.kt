package jumpaku.curves.fsc.generate

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import org.apache.commons.math3.linear.CholeskyDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix


class Generator(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val fillSpan: Double = knotSpan / degree,
        val extendInnerSpan: Double = knotSpan * 2,
        val extendOuterSpan: Double = knotSpan * 2,
        val extendDegree: Int = 2,
        val fuzzifier: Fuzzifier = Fuzzifier.Linear(
                velocityCoefficient = 0.0086,
                accelerationCoefficient = 0.0077)
) {

    init {
        require(degree >= 0)
        require(knotSpan > 0.0)
        require(fillSpan > 0.0)
        require(extendInnerSpan > 0.0)
        require(extendOuterSpan > 0.0)
        require(extendDegree >= 0)
    }

    fun generate(drawingStroke: DrawingStroke): BSpline = generate(drawingStroke.inputData)

    fun generate(data: List<ParamPoint>, weights: List<Double> = data.map { 1.0 }): BSpline =
            generate(data.zip(weights, ::WeightedParamPoint))

    fun generate(data: List<WeightedParamPoint>): BSpline {
        val sorted = data.sortedBy { it.param }
        val domain = Interval(sorted.first().param, sorted.last().param)
        val prepared = sorted
                .let { fill(it, fillSpan) }
                .let { extendBack(it, extendInnerSpan, extendOuterSpan, extendDegree) }
                .let { extendFront(it, extendInnerSpan, extendOuterSpan, extendDegree) }
        val domainExtended = Interval(prepared.first().param, prepared.last().param)
        val kv = KnotVector.clamped(domainExtended, degree, knotSpan)
        return generate(prepared, kv, fuzzifier).restrict(domain)
    }

    companion object {

        fun generate(data: List<WeightedParamPoint>, knotVector: KnotVector, fuzzifier: Fuzzifier): BSpline {
            val d = createPointDataMatrix(data)
            val (b, wbt) = createModelMatrixAndWeightedTransposed(data, knotVector)
            val solver = CholeskyDecomposition(wbt.multiply(b), 1e-10, 1e-10).solver
            val crispCp = solver.solve(wbt.multiply(d)).let { it.data.map { (x, y, z) -> Point.xyz(x, y, z) } }
            val f = createFuzzinessDataMatrix(data, BSpline(crispCp, knotVector), fuzzifier)
            val fuzziness = solver.solve(wbt.multiply(f)).let { it.data.map { it[0].coerceAtLeast(1e-10) } }
            val fuzzyCp = crispCp.zip(fuzziness) { p, r -> p.copy(r = r) }
            return BSpline(fuzzyCp, knotVector)
        }

        private fun createPointDataMatrix(data: List<WeightedParamPoint>): RealMatrix =
                MatrixUtils.createRealMatrix(data.size, 3).apply {
                    data.forEachIndexed { i, d -> d.run { setRow(i, point.toDoubleArray()) } }
                }

        private fun createFuzzinessDataMatrix(data: List<WeightedParamPoint>, crisp: BSpline, fuzzifier: Fuzzifier): RealMatrix =
                MatrixUtils.createRealMatrix(data.size, 1).apply {
                    setColumn(0, fuzzifier.fuzzify(crisp, data.map { it.param }).toDoubleArray())
                }

        private fun createModelMatrixAndWeightedTransposed(data: List<WeightedParamPoint>, knotVector: KnotVector)
                : Pair<OpenMapRealMatrix, OpenMapRealMatrix> {
            val cpSize = knotVector.extractedKnots.size - knotVector.degree - 1
            val b = OpenMapRealMatrix(data.size, cpSize)
            val wbt = OpenMapRealMatrix(cpSize, data.size)
            data.forEachIndexed { i, d ->
                val l = if (d.param >= knotVector.domain.end) (cpSize - 1)
                else knotVector.searchLastExtractedLessThanOrEqualTo(d.param)

                ((l - knotVector.degree)..l).forEach { j ->
                    val value = BSpline.basis(d.param, j, knotVector)
                    b.setEntry(i, j, value)
                    wbt.setEntry(j, i, value * d.weight)
                }
            }
            return b to wbt
        }

    }
}

