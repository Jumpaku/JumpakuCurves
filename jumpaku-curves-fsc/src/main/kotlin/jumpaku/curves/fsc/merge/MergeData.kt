package jumpaku.curves.fsc.merge

import jumpaku.commons.control.Option
import jumpaku.commons.control.toOption
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.generate.fit.weighted

class MergeData(
        val grade: Grade,
        val front: List<ParamPoint>,
        val back: List<ParamPoint>,
        val merged: List<WeightedParamPoint>) {

    val aggregated: List<WeightedParamPoint> = ((front + back).map { it.weighted(1.0) } + merged)
            .sortedBy { it.param }

    val domain: Interval = aggregated.run { Interval(first().param, last().param) }

    val mergeInterval: Interval = merged.run { Interval(first().param, last().param) }

    val frontInterval: Option<Interval> =
            front.takeIf { it.isNotEmpty() }?.run { Interval(first().param, last().param) }.toOption()

    val backInterval: Option<Interval> =
            back.takeIf { it.isNotEmpty() }?.run { Interval(first().param, last().param) }.toOption()

    companion object {

        fun parameterize(
                existing: List<ParamPoint>,
                overlapping: List<ParamPoint>,
                mergeRate: Double,
                overlapState: OverlapState
        ): MergeData {
            require(existing.size == overlapState.osm.rowSize)
            require(overlapping.size == overlapState.osm.columnSize)
            require(mergeRate in 0.0..1.0)

            val mergedData = overlapState.range
                    .map { (i, j) -> existing[i].lerp(mergeRate, overlapping[j]).weighted(overlapState.osm[i, j].value) }
                    .sortedBy { it.param }

            val (ridgeBeginI, ridgeBeginJ) = overlapState.ridge.first()
            val ridgeBeginExist = existing[ridgeBeginI].param
            val ridgeBeginOverlap = overlapping[ridgeBeginJ].param
            val frontExistCount = (0 until ridgeBeginI)
                    .takeWhile { (it to 0) !in overlapState.range }.size
            val eFront = existing.take(frontExistCount)
                    .map { it.run { copy(param = param + mergeRate * (ridgeBeginOverlap - ridgeBeginExist)) } }
            val frontOverlapCount = (0 until ridgeBeginJ)
                    .takeWhile { (0 to it) !in overlapState.range }.size
            val oFront = overlapping.take(frontOverlapCount)
                    .map { it.run { copy(param = param - (1 - mergeRate) * (ridgeBeginOverlap - ridgeBeginExist)) } }

            val (ridgeEndI, ridgeEndJ) = overlapState.ridge.last()
            val ridgeEndExist = existing[ridgeEndI].param
            val ridgeEndOverlap = overlapping[ridgeEndJ].param
            val backExistCount = (existing.lastIndex downTo (ridgeEndI + 1))
                    .takeWhile { (it to overlapping.lastIndex) !in overlapState.range }.size
            val eBack = existing.takeLast(backExistCount)
                    .map { it.run { copy(param = param + mergeRate * (ridgeEndOverlap - ridgeEndExist)) } }
            val backOverlapCount = (overlapping.lastIndex downTo (ridgeEndJ + 1))
                    .takeWhile { (existing.lastIndex to it) !in overlapState.range }.size
            val oBack = overlapping.takeLast(backOverlapCount)
                    .map { it.run { copy(param = param - (1 - mergeRate) * (ridgeEndOverlap - ridgeEndExist)) } }

            return MergeData(overlapState.grade, eFront + oFront, eBack + oBack, mergedData)
        }

        fun resample(
                existing: BSpline,
                overlapping: BSpline,
                mergeRate: Double,
                samplingSpan: Double,
                overlapState: OverlapState
        ): List<ParamPoint> {
            require(mergeRate in 0.0..1.0)
            val rowLast = overlapState.osm.rowLastIndex
            val columnLast = overlapState.osm.columnLastIndex
            val eSamples = existing.sample(rowLast + 1)
            val oSamples = overlapping.sample(columnLast + 1)

            val ridge = overlapState.ridge
            val (eMergeIdxBegin, oMergeIdxBegin) = ridge.first()
            val (eMergeIdxEnd, oMergeIdxEnd) = ridge.last()
            val (t0, t1, t2, t3) = listOf(0, eMergeIdxBegin, eMergeIdxEnd, rowLast).map { eSamples[it].param }
            val (s0, s1, s2, s3) = listOf(0, oMergeIdxBegin, oMergeIdxEnd, columnLast).map { oSamples[it].param }

            val u1 = t1.lerp(mergeRate, s1)
            val u2 = t2.lerp(mergeRate, s2)

            val eData = eSamples.mapNotNull {
                when (val t = it.param) {
                    in t1..t2 -> null
                    in t0..t1 -> it.copy(param = u1 - (t1 - t))
                    in t2..t3 -> it.copy(param = u2 + (t - t2))
                    else -> error("")
                }
            }
            val oData = oSamples.mapNotNull {
                when (val s = it.param) {
                    in s1..s2 -> null
                    in s0..s1 -> it.copy(param = u1 - (s1 - s))
                    in s2..s3 -> it.copy(param = u2 + (s - s2))
                    else -> error("")
                }
            }

            val nSamples = Interval(u1, u2).sample(samplingSpan).size
            val mData = Interval(t1, t2).sample(nSamples).zip(Interval(s1, s2).sample(nSamples)) { t, s ->
                ParamPoint(existing(t).lerp(mergeRate, overlapping(s)),  t.lerp(mergeRate, s))
            }
            return (eData + oData + mData).sortedBy { it.param }

/*
            class TransformParam(val s2: Double, val s3: Double, val t0: Double, val t1: Double, val t2: Double, val t3: Double, val t4: Double, val t5: Double) {
                val mergeGrad = (s3 - s2).divOrDefault(t3 - t2) { 1.0 }
                val aFront = 0.5 * (mergeGrad - 1) / (t2 - t1)
                val bFront = 1 - 2 * aFront * t1
                val cFront = s2 - aFront * t2 * t2 - bFront * t2
                val aBack = 0.5 * (mergeGrad - 1) / (t3 - t4)
                val bBack = 1 - 2 * aBack * t4
                val cBack = s3 - aBack * t3 * t3 - bBack * t3
                operator fun invoke(t: Double) = when (t) {
                    in t0..t1 -> t + s2 - t2
                    in t1..t2 -> aFront * t * t + bFront * t + cFront
                    in t2..t3 -> mergeGrad * t + s2 - mergeGrad * t2
                    in t3..t4 -> aBack * t * t + bBack * t + cBack
                    in t4..t5 -> t + s3 - t3
                    else -> error("")
                }
            }

            val range = overlapState.range
            val ridge = overlapState.ridge
            val (eMergeIdxBegin, oMergeIdxBegin) = ridge.first()
            val (eMergeIdxEnd, oMergeIdxEnd) = ridge.last()
            val eFrontRemainIdx = (0 until eMergeIdxBegin).lastOrNull { i -> (i to 0) !in range }
            val oFrontRemainIdx = (0 until oMergeIdxBegin).lastOrNull { j -> (0 to j) !in range }
            val eBackRemainIdx = (rowLast downTo (eMergeIdxEnd + 1)).lastOrNull { i -> (i to columnLast) !in range }
            val oBackRemainIdx = (columnLast downTo (oMergeIdxEnd + 1)).lastOrNull { j -> (rowLast to j) !in range }

            val eT0 = existing[0].param
            val eT1 = existing[eFrontRemainIdx ?: 0].param
            val eT2 = existing[eMergeIdxBegin].param
            val eT3 = existing[eMergeIdxEnd].param
            val eT4 = existing[eBackRemainIdx ?: rowLast].param
            val eT5 = existing[rowLast].param
            val oT0 = overlapping[0].param
            val oT1 = overlapping[oFrontRemainIdx ?: 0].param
            val oT2 = overlapping[oMergeIdxBegin].param
            val oT3 = overlapping[oMergeIdxEnd].param
            val oT4 = overlapping[oBackRemainIdx ?: columnLast].param
            val oT5 = overlapping[columnLast].param

            val s2 = eT2.lerp(mergeRate, oT2)
            val s3 = eT3.lerp(mergeRate, oT3)

            val eTransform = TransformParam(s2, s3, eT0, eT1, eT2, eT3, eT4, eT5)
            val eData = existing.map { it.copy(param = eTransform(it.param)) }

            val oTransform = TransformParam(s2, s3, oT0, oT1, oT2, oT3, oT4, oT5)
            val oData = overlapping.map { it.copy(param = oTransform(it.param)) }

            val mergedData = ridge.map { (i, j) -> existing[i].lerp(mergeRate, overlapping[j]).weighted() }

            return MergeData(overlapState.grade,
                    eData.take(eMergeIdxBegin) + oData.take(oMergeIdxBegin),
                    eData.drop(eMergeIdxEnd + 1) + oData.drop(oMergeIdxEnd + 1),
                    mergedData)

 */
        }
    }
}

