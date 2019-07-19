package jumpaku.curves.experimental.demo.edit.oldblend

enum class OverlapType {
    ExistOverlap,
    OverlapExist,
    ExistOverlapExist,
    OverlapExistOverlap;

    companion object {

        fun judgeType(osmRowSize: Int, osmColumnSize: Int, pathElements: List<Pair<Int, Int>>): OverlapType {
            val existingLast = osmRowSize - 1
            val overlappingLast = osmColumnSize - 1
            val (beginI, beginJ) = pathElements.first()
            val (endI, endJ) = pathElements.last()

            return when {
                beginI == 0 && endI == existingLast -> OverlapExistOverlap
                beginI == 0 && endJ == overlappingLast -> OverlapExist
                beginJ == 0 && endI == existingLast -> ExistOverlap
                beginJ == 0 && endJ == overlappingLast -> ExistOverlapExist
                else -> error("no overlap")
            }
        }
    }
}