package jumpaku.curves.fsc.generate

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
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
        val dataPreparer: DataPreparer = DataPreparer(knotSpan / degree, knotSpan * 2, knotSpan * 2, 2),
        val fuzzifier: Fuzzifier = Fuzzifier.Linear(0.025, 0.001)
) : ToJson {

    fun generate(drawingStroke: DrawingStroke): BSpline = generate(drawingStroke.inputData)

    fun generate(data: List<ParamPoint>, weights: List<Double> = data.map { 1.0 }): BSpline =
            generate(data.zip(weights, ::WeightedParamPoint))

    fun generate(data: List<WeightedParamPoint>): BSpline {
        val domain = Interval(data.first().param, data.last().param)
        val prepared = dataPreparer.prepare(data)
        val domainExtended = Interval(prepared.first().param, prepared.last().param)
        val kv = KnotVector.clamped(domainExtended, degree, domainExtended.sample(knotSpan).size + degree * 2)
        return generate(prepared, kv).restrict(domain)
    }

    fun generate(data: List<WeightedParamPoint>, knotVector: KnotVector): BSpline {
        val d = createPointDataMatrix(data)
        val (b, wbt) = createModelMatrixAndWeightedTransposed(data, knotVector)
        val solver = CholeskyDecomposition(wbt.multiply(b), 1e-10, 1e-10).solver
        val crispCp = solver.solve(wbt.multiply(d)).let { it.data.map { Point.xyz(it[0], it[1], it[2]) } }
        val f = createFuzzinessDataMatrix(data, BSpline(crispCp, knotVector))
        val fuzziness = solver.solve(wbt.multiply(f)).let { it.data.map { it[0].coerceAtLeast(0.0) } }
        val fuzzyCp = crispCp.zip(fuzziness) { p, r -> p.copy(r = r) }
        return BSpline(fuzzyCp, knotVector)
    }

    internal fun createPointDataMatrix(data: List<WeightedParamPoint>): RealMatrix =
            MatrixUtils.createRealMatrix(data.size, 3).apply {
                data.forEachIndexed { i, d -> d.run { setRow(i, point.toDoubleArray()) } }
            }

    internal fun createFuzzinessDataMatrix(data: List<WeightedParamPoint>, crisp: BSpline): RealMatrix =
            MatrixUtils.createRealMatrix(data.size, 1).apply {
                setColumn(0, fuzzifier.fuzzify(crisp, data.map { it.param }).toDoubleArray())
            }

    internal fun createModelMatrixAndWeightedTransposed(data: List<WeightedParamPoint>, knotVector: KnotVector)
            : Pair<OpenMapRealMatrix, OpenMapRealMatrix> {
        val cpSize = knotVector.extractedKnots.size - knotVector.degree - 1
        val b = OpenMapRealMatrix(data.size, cpSize)
        val wbt = OpenMapRealMatrix(cpSize, data.size)
        data.forEachIndexed { i, d ->
            val l = if (d.param >= knotVector.domain.end) (cpSize - 1)
            else knotVector.searchLastExtractedLessThanOrEqualTo(d.param)

            ((l - degree)..l).forEach { j ->
                val value = BSpline.basis(d.param, j, knotVector)
                b.setEntry(i, j, value)
                wbt.setEntry(j, i, value * d.weight)
            }
        }
        return b to wbt
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "degree" to degree.toJson(),
            "knotSpan" to knotSpan.toJson(),
            "dataPreparer" to dataPreparer.toJson(),
            "fuzzifier" to fuzzifier.toJson())

    companion object {

        fun fromJson(json: JsonElement): Generator = Generator(
                json["degree"].int,
                json["knotSpan"].double,
                DataPreparer.fromJson(json["dataPreparer"]),
                Fuzzifier.fromJson(json["fuzzifier"]))
    }
}