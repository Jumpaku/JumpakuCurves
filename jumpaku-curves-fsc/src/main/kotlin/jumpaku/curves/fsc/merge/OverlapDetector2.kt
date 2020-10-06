package jumpaku.curves.fsc.merge

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.merge.OverlapDetector.Companion.collectRange
import jumpaku.curves.fsc.merge.OverlapDetector.Companion.findRidge


class OverlapRidge(val grade: Grade, val ridge: List<Pair<Int, Int>>)

class OverlapDetector2 {

    fun detectBaseRidge(
            existSamples: List<ParamPoint>,
            overlapSamples: List<ParamPoint>,
            mergeRate: Double,
            overlapThreshold: Grade
    ): Pair<OverlapMatrix, Option<OverlapRidge>> {
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val maxDistRidge = findRidge(osm, compareBy { it.dist(mergeRate) }) { i, j -> osm[i, j] > overlapThreshold }
                .map { OverlapRidge(it.grade, it.subRidge.map { it.asPair() }) }
                .orNull() ?: return osm to None
        return osm to Some(maxDistRidge)
    }

    fun detectDerivedRidge(
            osm: OverlapMatrix,
            baseRidge: OverlapRidge,
            mergeRate: Double,
            overlapThreshold: Grade
    ): Option<OverlapRidge> {
        if (overlapThreshold > baseRidge.grade) return None
        val available = collectRange(osm, baseRidge.ridge, overlapThreshold)
        val maxDistRidge = findRidge(osm, compareBy { it.dist(mergeRate) }) { i, j -> (i to j) in available }
                .map { OverlapRidge(it.grade, it.subRidge.map { it.asPair() }) }
                .orNull() ?: return  None
        return Some(maxDistRidge)
    }
}
