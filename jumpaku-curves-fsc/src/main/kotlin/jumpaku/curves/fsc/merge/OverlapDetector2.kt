package jumpaku.curves.fsc.merge

import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.merge.OverlapDetector.Companion.collectRange


class OverlapRidge(val grade: Grade, val ridge: List<Pair<Int, Int>>) : List<Pair<Int, Int>> by ridge

sealed class OverlapState2(val osm: OverlapMatrix) {
    class NotFound(osm: OverlapMatrix) : OverlapState2(osm)
    class Found(
            osm: OverlapMatrix,
            val coreRidge: OverlapRidge,
            val transitionBegin: Pair<Int, Int>,
            val transitionEnd: Pair<Int, Int>
    ) : OverlapState2(osm)
}

class OverlapDetector2(val overlapThreshold: Grade, val mergeRate: Double) {

    fun detect(existSamples: List<ParamPoint>, overlapSamples: List<ParamPoint>): OverlapState2 {
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val (overlapBegin, overlapEnd) = DpHelper.findRidgeBeginEnd(osm, mergeRate) { (i, j) -> osm[i, j] > overlapThreshold }
                .orNull() ?: return OverlapState2.NotFound(osm)
        val coreRidge = DpHelper.findRidge(osm, overlapBegin, overlapEnd) { (i, j) -> osm[i, j] > overlapThreshold }
                .orNull() ?: return OverlapState2.NotFound(osm)
        val available = collectRange(osm, coreRidge, Grade.FALSE)
        val (transitionBegin, transitionEnd) = DpHelper.findRidgeBeginEnd(osm, mergeRate) { it.asPair() in available }
                .orNull() ?: return OverlapState2.NotFound(osm)
        return OverlapState2.Found(osm, coreRidge, transitionBegin.asPair(), transitionEnd.asPair())
    }
}

