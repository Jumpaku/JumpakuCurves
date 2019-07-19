package jumpaku.curves.experimental.demo.edit.oldblend

import jumpaku.curves.core.fuzzy.Grade

class OverlapPath(val type: OverlapType, val grade: Grade, indexPairs: Iterable<Pair<Int, Int>>)
    : AbstractList<Pair<Int, Int>>() {

    private val indexPairs: List<Pair<Int, Int>> = indexPairs.toList()

    override val size: Int = this.indexPairs.size

    override fun get(index: Int): Pair<Int, Int> = indexPairs[index]
}

