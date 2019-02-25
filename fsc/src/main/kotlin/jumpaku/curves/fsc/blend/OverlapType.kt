package jumpaku.fsc.blend

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
                beginI == 0 && endI == existingLast -> OverlapType.OverlapExistOverlap
                beginI == 0 && endJ == overlappingLast -> OverlapType.OverlapExist
                beginJ == 0 && endI == existingLast -> OverlapType.ExistOverlap
                beginJ == 0 && endJ == overlappingLast -> OverlapType.ExistOverlapExist
                else -> error("")
            }
        }
    }}