package jumpaku.curves.fsc.blend

import jumpaku.curves.core.fuzzy.Grade

class OverlapState(
        val osm: OverlapMatrix,
        val grade: Grade,
        val ridge: List<Pair<Int, Int>>,
        val range: Set<Pair<Int, Int>>
) {
    init {
        require(ridge.isEmpty() == range.isEmpty())
    }

    fun isEmpty(): Boolean = ridge.isEmpty() && range.isEmpty()
}