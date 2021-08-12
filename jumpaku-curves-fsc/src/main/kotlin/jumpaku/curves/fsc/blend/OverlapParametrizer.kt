package jumpaku.curves.fsc.blend

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.transformParams
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.geom.times
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.generate.fit.weighted
import kotlin.math.abs
import kotlin.math.ceil

class BlendPair(
    val existing: ParamPoint,
    val overlapping: ParamPoint,
    val blendRate: Double
) {
    fun blend(): WeightedParamPoint =
        existing.lerp(blendRate, overlapping).weighted(/*existing.point.isPossible(overlapping.point).value*/)
}

class OverlapParametrizer(val samplingSpan: Double, val blendRate: Double) {

    private fun resample0(
        existingSampled: Blender.SampledCurve,
        overlappingSampled: Blender.SampledCurve,
        overlapState: OverlapState.Detected
    ): List<BlendPair> {
        val existingSpans0 = existingSampled.spans
            .take(overlapState.run { (front + middle).firstOrNull()?.row ?: 0 })
        val overlappingSpans0 = overlappingSampled.spans
            .take(overlapState.run { (front + middle).firstOrNull()?.column ?: 0 })
        return when {
            existingSpans0.isEmpty() && overlappingSpans0.isEmpty() -> emptyList()
            existingSpans0.isNotEmpty() -> existingSampled.curve.sample(existingSpans0.run {
                Interval(first().begin, last().end).sample(samplingSpan)
            }).map { BlendPair(it, it, 1.0) }
            overlappingSpans0.isNotEmpty() -> overlappingSampled.curve.sample(overlappingSpans0.run {
                Interval(first().begin, last().end).sample(samplingSpan)
            }).map { BlendPair(it, it, 1.0) }
            else -> error("")
        }
    }

    fun stretchAccordingToChordLength(data: List<BlendPair>): List<BlendPair> {
        val l0 = data.zipWithNext { a, b -> a.existing.point.dist(b.existing.point) }.sum()
        val l1 = data.zipWithNext { a, b -> a.blend().point.dist(b.blend().point) }.sum()
        val domain = data.run { Interval(first().existing.param, last().existing.param) }
        val range = Interval(0.0, domain.span * l1 / l0)
        return data.map { d ->
            val t = 0.0.lerp((d.existing.param - domain.begin) / domain.span, range.span).coerceIn(range)
            BlendPair(d.existing.copy(param = t), d.overlapping.copy(param = t), d.blendRate)
        }
    }

    private fun resample1(
        existingSampled: Blender.SampledCurve,
        overlappingSampled: Blender.SampledCurve,
        overlapState: OverlapState.Detected
    ): List<BlendPair> {
        val begin = overlapState.middle.first()
        val existingSpans1 = integrateSpans(existingSampled.spans, overlapState.front, { it.row })
        val overlappingSpans1 = integrateSpans(overlappingSampled.spans, overlapState.front, { it.column })
        return when {
            begin == OverlapMatrix.Key(0, 0) -> emptyList()
            begin.column == 0 && existingSpans1.isEmpty() -> emptyList()
            begin.row == 0 && overlappingSpans1.isEmpty() -> emptyList()
            begin.column == 0 -> {
                val existingParams = existingSpans1.run { Interval(first().begin, last().end).sample(samplingSpan) }
                val existingSamples = existingSampled.curve.sample(existingParams)
                val overlappingBegin = overlappingSampled.curve.run { invoke(domain.begin) }
                val diff = overlappingBegin - existingSamples.last().point
                val n = existingSamples.size
                val data = existingSamples.mapIndexed { i, p ->
                    val q = p.copy(point = (p.point + diff).copy(r = p.point.r))
                    BlendPair(p, q, i * blendRate / (n - 1.0))
                }
                stretchAccordingToChordLength(data)
            }
            begin.row == 0 -> {
                val overlappingParams =
                    overlappingSpans1.run { Interval(first().begin, last().end).sample(samplingSpan) }
                val overlappingSamples = overlappingSampled.curve.sample(overlappingParams)
                val existingBegin = existingSampled.curve.run { invoke(domain.begin) }
                val diff = existingBegin - overlappingSamples.last().point
                val n = overlappingSamples.size
                val data = overlappingSamples.mapIndexed { i, p ->
                    val q = p.copy(point = (p.point + diff).copy(r = p.point.r))
                    BlendPair(q, p, (i * blendRate + n - i - 1) / (n - 1))
                }
                stretchAccordingToChordLength(data)

            }
            else -> error("")
        }
    }

    private fun resample2(
        existingSampled: Blender.SampledCurve,
        overlappingSampled: Blender.SampledCurve,
        overlapState: OverlapState.Detected
    ): List<BlendPair> {
        val existingSpans2 = integrateSpans(existingSampled.spans, overlapState.middle, { it.row })
        val overlappingSpans2 = integrateSpans(overlappingSampled.spans, overlapState.middle, { it.column })
        return existingSpans2.zip(overlappingSpans2) { existingSpan, overlappingSpan ->
            val blendSpan = existingSpan.span.lerp(blendRate, overlappingSpan.span)
            val nSamples = Interval(0.0, blendSpan).sample(samplingSpan).size
            val existingSamples = existingSampled.curve.sample(existingSpan.sample(nSamples))
            val overlappingSamples = overlappingSampled.curve.sample(overlappingSpan.sample(nSamples))
            existingSamples.zip(overlappingSamples) { e, o -> BlendPair(e, o, blendRate) }
        }.flatten()
    }

    private fun resample3(
        existingSampled: Blender.SampledCurve,
        overlappingSampled: Blender.SampledCurve,
        overlapState: OverlapState.Detected
    ): List<BlendPair> {
        val end = overlapState.middle.last()
        val existingSpans3 = integrateSpans(existingSampled.spans, overlapState.back, { it.row })
        val overlappingSpans3 = integrateSpans(overlappingSampled.spans, overlapState.back, { it.column })
        return when {
            end == overlapState.osm.run { OverlapMatrix.Key(rowLastIndex, columnLastIndex) } -> emptyList()
            end.column == overlapState.osm.columnLastIndex && existingSpans3.isEmpty() -> emptyList()
            end.row == overlapState.osm.rowLastIndex && overlappingSpans3.isEmpty() -> emptyList()
            end.column == overlapState.osm.columnLastIndex -> {
                val existingParams = existingSpans3.run { Interval(first().begin, last().end).sample(samplingSpan) }
                val existingSamples = existingSampled.curve.sample(existingParams)
                val overlappingEnd = overlappingSampled.curve.run { invoke(domain.end) }
                val diff = overlappingEnd - existingSamples.first().point
                val n = existingSamples.size
                val data = existingSamples.mapIndexed { i, p ->
                    val q = p.copy(point = (p.point + diff).copy(r = p.point.r))
                    BlendPair(p, q, (n - i - 1) * blendRate / (n - 1.0))
                }
                stretchAccordingToChordLength(data)

            }
            end.row == overlapState.osm.rowLastIndex -> {
                val overlappingParams =
                    overlappingSpans3.run { Interval(first().begin, last().end).sample(samplingSpan) }
                val overlappingSamples = overlappingSampled.curve.sample(overlappingParams)
                val existingEnd = existingSampled.curve.run { invoke(domain.end) }
                val diff = existingEnd - overlappingSamples.first().point
                val n = overlappingSamples.size
                val data = overlappingSamples.mapIndexed { i, p ->
                    val q = p.copy(point = (p.point + diff).copy(r = p.point.r))
                    BlendPair(q, p, ((n - i - 1) * blendRate + i) / (n - 1))
                }
                stretchAccordingToChordLength(data)
            }
            else -> error("")
        }
    }

    private fun resample4(
        existingSampled: Blender.SampledCurve,
        overlappingSampled: Blender.SampledCurve,
        overlapState: OverlapState.Detected
    ): List<BlendPair> {
        val existingSpans4 = existingSampled.spans
            .drop(overlapState.run { ((middle + back).lastOrNull()?.row ?: existingSampled.spans.lastIndex) + 1 })
        val overlappingSpans4 = overlappingSampled.spans
            .drop(overlapState.run { ((middle + back).lastOrNull()?.column ?: overlappingSampled.spans.lastIndex) + 1 })
        return when {
            existingSpans4.isEmpty() && overlappingSpans4.isEmpty() -> emptyList()
            existingSpans4.isNotEmpty() -> existingSampled.curve.sample(existingSpans4.run {
                Interval(first().begin, last().end).sample(samplingSpan)
            }).map { BlendPair(it, it, 1.0) }
            overlappingSpans4.isNotEmpty() -> overlappingSampled.curve.sample(overlappingSpans4.run {
                Interval(first().begin, last().end).sample(samplingSpan)
            }).map { BlendPair(it, it, 1.0) }
            else -> error("")
        }
    }

    fun parametrize(
        existingSampled: Blender.SampledCurve,
        overlappingSampled: Blender.SampledCurve,
        overlapState: OverlapState.Detected
    ): List<WeightedParamPoint> {
        val data2 = resample2(existingSampled, overlappingSampled, overlapState).map { it.blend() }
        val data1 = resample1(existingSampled, overlappingSampled, overlapState).map { it.blend() }
            .let { if (it.isEmpty()) emptyList() else shiftParams(it, data2.first().param - it.last().param) }
        val data3 = resample3(existingSampled, overlappingSampled, overlapState).map { it.blend() }
            .let { if (it.isEmpty()) emptyList() else shiftParams(it, data2.last().param - (it.first().param)) }
        val data0 = resample0(existingSampled, overlappingSampled, overlapState).map { it.blend() }
            .let {
                if (it.isEmpty()) emptyList()
                else shiftParams(it, (data1 + data2).first().param - it.last().param)
            }
        val data4 = resample4(existingSampled, overlappingSampled, overlapState).map { it.blend() }
            .let {
                if (it.isEmpty()) emptyList()
                else shiftParams(it, (data2 + data3).last().param - it.first().param)
            }
        return listOf(data0, data1, data2, data3, data4).flatten()
    }


    companion object {

        fun integrateSpans(
            spans: List<Blender.SmallInterval>,
            keys: List<OverlapMatrix.Key>,
            getIndex: (OverlapMatrix.Key) -> Int
        ): List<Interval> {
            return mutableListOf<Interval>().apply {
                var prevColumn = -1
                var prevRow = -1
                for (key in keys) {
                    val span = spans[getIndex(key)]
                    if (key.row == prevRow || key.column == prevColumn) {
                        set(lastIndex, last().copy(end = span.end))
                    } else {
                        add(span.run { Interval(begin, end) })
                    }
                    prevColumn = key.column
                    prevRow = key.row
                }
            }
        }

        fun shiftParams(paramPoints: List<WeightedParamPoint>, delta: Double): List<WeightedParamPoint> =
            if (paramPoints.isEmpty()) emptyList()
            else paramPoints.map { WeightedParamPoint(it.point, it.param + delta, it.weight) }
    }
}