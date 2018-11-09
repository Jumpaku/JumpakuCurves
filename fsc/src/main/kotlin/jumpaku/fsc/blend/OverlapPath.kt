package jumpaku.fsc.blend

import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.Option
import jumpaku.core.util.optionWhen

class OverlapPath(val type: OverlapType, val grade: Grade, indexPairs: Iterable<Pair<Int, Int>>)
    : AbstractList<Pair<Int, Int>>() {

    private val indexPairs: List<Pair<Int, Int>> = indexPairs.toList()

    override val size: Int = this.indexPairs.size

    override fun get(index: Int): Pair<Int, Int> = indexPairs[index]
}