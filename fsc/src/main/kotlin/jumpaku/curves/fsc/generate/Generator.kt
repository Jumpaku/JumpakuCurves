package jumpaku.curves.fsc.generate

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
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

    fun generate(data: List<ParamPoint>): BSpline {
        val domain = Interval(data.first().param, data.last().param)
        val prepared = preparer.prepare(data)
        val domainExtended = Interval(prepared.first().param, prepared.last().param)
        val kv = KnotVector.clamped(domainExtended, degree, domainExtended.sample(knotSpan).size + degree * 2)
        val d = createPointDataMatrix(prepared)
        val (b, bt) = createModelMatrixAndTransposed(prepared, kv)
        val solver = CholeskyDecomposition(bt.multiply(b), 1e-10, 1e-10).solver
        val crispCp = solver.solve(bt.multiply(d)).let { it.data.map { Point.xyz(it[0], it[1], it[2]) } }
        val f = createFuzzinessDataMatrix(prepared, BSpline(crispCp, kv))
        val fuzziness = solver.solve(bt.multiply(f)).let { it.data.map { it[0].coerceAtLeast(0.0) } }
        val fuzzyCp = crispCp.zip(fuzziness) { p, r -> p.copy(r = r) }
        return BSpline(fuzzyCp, kv).restrict(domain)
    }

    fun createPointDataMatrix(data: List<ParamPoint>): RealMatrix {
        val d = MatrixUtils.createRealMatrix(data.size, 3)
        data.forEachIndexed { i, (point, _) -> d.setRow(i, point.toDoubleArray()) }
        return d
    }

    fun createFuzzinessDataMatrix(data: List<ParamPoint>, crisp: BSpline): RealMatrix {
        val f = MatrixUtils.createRealMatrix(data.size, 1)
        f.setColumn(0, fuzzifier.fuzzify(crisp, data.map { it.param }).toDoubleArray())
        return f
    }

    fun createModelMatrixAndTransposed(data: List<ParamPoint>, knotVector: KnotVector)
            : Pair<OpenMapRealMatrix, OpenMapRealMatrix> {
        val cpSize = knotVector.extractedKnots.size - knotVector.degree - 1
        val b = OpenMapRealMatrix(data.size, cpSize)
        val bt = OpenMapRealMatrix(cpSize, data.size)
        data.forEachIndexed { i, (_, param) ->
            val l = if (param >= knotVector.domain.end) (cpSize - 1)
            else knotVector.searchLastExtractedLessThanOrEqualTo(param)

            ((l - degree)..l).forEach { j ->
                val value = BSpline.basis(param, j, knotVector)
                b.setEntry(i, j, value)
                bt.setEntry(j, i, value)
            }
        }
        return b to bt
    }
}