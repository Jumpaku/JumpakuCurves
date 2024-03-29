package jumpaku.curves.fsc.generate

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import org.apache.commons.math3.linear.*
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs

/**
 * Generates an FSC from a DrawingStroke.
 * The concept of this process is proposed in the following papers:
 * - SAGA, S, MAKINO, H, Jun-ichi, S. A method for modeling freehand curves - the fuzzy spline interpolation - (in japanese). The Transactions of the Institute of Electronics, Information and Communication Engineers 1994;J77-D-II(8):1610–1619. URL: https://ci.nii.ac.jp/naid/110003228496/en/
 * - Saga, S, Makino, H. Fuzzy spline interpolation and its application to online freehand curve identification. In: Proceedings 1993 Second IEEE International Conference on Fuzzy Systems; vol. 2. 1993, p. 1183–1190. DOI: doi:10.1109/FUZZY.1993.327560 URL: URL https://doi.org/10.1109/FUZZY.1993.327560
 */
class Generator(
    val degree: Int = 3,
    val knotSpan: Double = 0.1,
    val fillSpan: Double = knotSpan / degree,
    val extendInnerSpan: Double = knotSpan * 2,
    val extendOuterSpan: Double = knotSpan * 2,
    val extendDegree: Int = 2,
    val fuzzifier: Fuzzifier = Fuzzifier.Linear(
        velocityCoefficient = 0.0086,
        accelerationCoefficient = 0.0077
    )
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
        val filled = fill(sorted, fillSpan)
        val back = extendBack(filled, extendInnerSpan, extendOuterSpan, extendDegree, fillSpan)
        val front = extendFront(filled, extendInnerSpan, extendOuterSpan, extendDegree, fillSpan)
        val prepared = front + filled + back
        /*.let { fill(it, fillSpan) }
        .let { extendBack(it, extendInnerSpan, extendOuterSpan, extendDegree) }
        .let { extendFront(it, extendInnerSpan, extendOuterSpan, extendDegree) }*/
        val domainExtended = Interval(prepared.first().param, prepared.last().param)
        val kv = KnotVector.clamped(domainExtended, degree, knotSpan)
        return generate(prepared, kv, fuzzifier).restrict(domain)
    }

    companion object {


        fun generate(data: List<WeightedParamPoint>, knotVector: KnotVector, fuzzifier: Fuzzifier): BSpline {
            val d = createPointDataMatrix(data)
            val (btwb, btw) = createModelMatrices(data, knotVector)
            val solver = CholeskyDecomposition(btwb, 1e-15, 1e-10).solver
            val btwd = btw.multiply(d)
            val cps = solver.solve(btwd).data
            val f = createFuzzinessDataMatrix(
                data,
                BSpline(cps.map { (x, y, z) -> Point.xyz(x, y, z) }, knotVector),
                fuzzifier
            )
            val btwf = btw.multiply(f)
            val rs = solver.solve(btwf).data
            val fuzzyCp = cps.indices
                .map { i -> Point.xyzr(cps[i][0], cps[i][1], cps[i][2], rs[i][0].coerceAtLeast(1e-10)) }
            return BSpline(fuzzyCp, knotVector)
        }

        private fun createPointDataMatrix(data: List<WeightedParamPoint>): RealMatrix =
            MatrixUtils.createRealMatrix(data.size, 3).apply {
                data.forEachIndexed { i, d -> d.run { setRow(i, point.toDoubleArray()) } }
            }

        private fun createFuzzinessDataMatrix(
            data: List<WeightedParamPoint>,
            crisp: BSpline,
            fuzzifier: Fuzzifier
        ): RealMatrix =
            MatrixUtils.createRealMatrix(fuzzifier.fuzzify(crisp, data.map { it.param }).map { doubleArrayOf(it) }
                .toTypedArray())

        private fun createModelMatrices(data: List<WeightedParamPoint>, knotVector: KnotVector)
                : Pair<OpenMapRealMatrix, OpenMapRealMatrix> {
            val cpSize = knotVector.size - knotVector.degree - 1
            val dSize = data.size
            val w = data.map { it.weight }
            val b = OpenMapRealMatrix(dSize, cpSize).apply {
                //var l = knotVector.degree
                data.forEachIndexed { i, (pt, _) ->
                    //while (l < cpSize-1 && pt.param >= knotVector[l + 1]) ++l
                    val t = pt.param
                    val l = if (t >= knotVector.domain.end) (cpSize - 1)
                    else knotVector.searchIndexToInsert(t)
                    ((l - knotVector.degree)..l).forEach { j ->
                        setEntry(i, j, BSpline.basis(pt.param, j, knotVector, l))
                    }
                }
            }
            val beginIdx = IntArray(cpSize).apply {
                set(0, 0)
                for (j in 1 until cpSize) {
                    set(j, (get(j - 1) until dSize).first { i -> b.getEntry(i, j) > 0 })
                }
            }
            val endIdx = IntArray(cpSize).apply {
                set(cpSize - 1, dSize - 1)
                for (j in (cpSize - 2) downTo 0) {
                    set(j, (get(j + 1) downTo 0).first { i -> b.getEntry(i, j) > 0 })
                }
            }
            val btwb = OpenMapRealMatrix(cpSize, cpSize).apply {
                for (i in 0 until cpSize) {
                    for (j in 0 until cpSize) {
                        val ks = max(beginIdx[i], beginIdx[j])..min(endIdx[i], endIdx[j])
                        if (ks.isEmpty()) continue
                        setEntry(i, j, ks.sumOf { k -> b.getEntry(k, i) * w[k] * b.getEntry(k, j) })
                    }
                }
            }
            val btw = OpenMapRealMatrix(cpSize, dSize).apply {
                for (i in 0 until cpSize) {
                    for (j in beginIdx[i]..endIdx[i]) {
                        setEntry(i, j, b.getEntry(j, i) * w[j])
                    }
                }
            }
            return btwb to btw
        }
    }
}

