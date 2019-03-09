package jumpaku.curves.fsc.generate

import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.DrawingStroke
import org.apache.commons.math3.linear.CholeskyDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix


class Generator(
        val degree: Int = 3,
        val knotSpan: Double = 0.075,
        val preparer: DataPreparer = DataPreparer(knotSpan/degree, knotSpan*2, knotSpan*2, 2),
        val fuzzifier: Fuzzifier = LinearFuzzifier(0.025, 0.001)
) {

    fun generate(drawingStroke: DrawingStroke): BSpline = generate(drawingStroke.inputData)

    fun generate(data: List<ParamPoint>, weights: List<Double> = data.map { 1.0 }): BSpline =
            generate(data.zip(weights, ::WeightedParamPoint))

    fun generate(data: List<WeightedParamPoint>): BSpline {
        val domain = Interval(data.first().param, data.last().param)
        val prepared = preparer.prepare(data)
        val domainExtended = Interval(prepared.first().param, prepared.last().param)
        val kv = KnotVector.clamped(domainExtended, degree, domainExtended.sample(knotSpan).size + degree * 2)
        val d = createPointDataMatrix(prepared)
        val (b, wbt) = createModelMatrixAndWeightedTransposed(prepared, kv)
        val solver = CholeskyDecomposition(wbt.multiply(b), 1e-10, 1e-10).solver
        val crispCp = solver.solve(wbt.multiply(d)).let { it.data.map { Point.xyz(it[0], it[1], it[2]) } }
        val f = createFuzzinessDataMatrix(prepared, BSpline(crispCp, kv))
        val fuzziness = solver.solve(wbt.multiply(f)).let { it.data.map { it[0].coerceAtLeast(0.0) } }
        val fuzzyCp = crispCp.zip(fuzziness) { p, r -> p.copy(r = r) }
        return BSpline(fuzzyCp, kv).restrict(domain)
    }

    private fun createPointDataMatrix(data: List<WeightedParamPoint>): RealMatrix =
            MatrixUtils.createRealMatrix(data.size, 3).apply {
                data.forEachIndexed { i, d -> d.run { setRow(i, point.toDoubleArray()) } }
            }

    private fun createFuzzinessDataMatrix(data: List<WeightedParamPoint>, crisp: BSpline): RealMatrix =
            MatrixUtils.createRealMatrix(data.size, 1).apply {
                setColumn(0, fuzzifier.fuzzify(crisp, data.map { it.param }).toDoubleArray())
            }

    private fun createModelMatrixAndWeightedTransposed(data: List<WeightedParamPoint>, knotVector: KnotVector)
            : Pair<OpenMapRealMatrix, OpenMapRealMatrix> {
        val cpSize = knotVector.extractedKnots.size - knotVector.degree - 1
        val b = OpenMapRealMatrix(data.size, cpSize)
        val wbt = OpenMapRealMatrix(cpSize, data.size)
        /*val paramCountMap = data.groupBy { it.param }.mapValues { it.value.size }
        val paramWeightMap = data.distinctBy { it.param }.mapIndexed { i, d ->
            d.param to when (i) {
                0 -> data[i + 1].param - d.param
                data.lastIndex -> d.param - data[i - 1].param
                else -> (data[i + 1].param - data[i - 1].param) / 2
            }
        }.toMap()*/
        data.forEachIndexed { i, d ->
            val l = if (d.param >= knotVector.domain.end) (cpSize - 1)
            else knotVector.searchLastExtractedLessThanOrEqualTo(d.param)

            ((l - degree)..l).forEach { j ->
                val value = BSpline.basis(d.param, j, knotVector)
                b.setEntry(i, j, value)
                wbt.setEntry(j, i, value * d.weight)// * paramWeightMap.getValue(d.param) / paramCountMap.getValue(d.param))
            }
        }
        return b to wbt
    }
}