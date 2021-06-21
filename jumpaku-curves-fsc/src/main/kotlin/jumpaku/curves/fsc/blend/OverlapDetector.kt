package jumpaku.curves.fsc.blend

import jumpaku.commons.control.*
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp
import java.lang.Integer.max
import java.util.*
import kotlin.Comparator
import kotlin.math.abs
import kotlin.random.Random


class OverlapDetector(val overlapThreshold: Grade, val blendRate: Double) {

    fun detect(
        existSpans: Blender.SampledCurve,
        overlapSpans: Blender.SampledCurve
    ): OverlapState {
        val osm = OverlapMatrix.create(existSpans.representativePoints, overlapSpans.representativePoints)
        val pathBeginEnd = findPathBeginEnd(existSpans.spans, overlapSpans.spans, osm).orNull()
            ?: return OverlapState.NotDetected(osm)
        val middlePath = findMiddlePath(osm, pathBeginEnd)
        val frontPath = findFront(osm, middlePath)
        val backPath = findBack(osm, middlePath)
        return OverlapState.Detected(osm, frontPath, middlePath, backPath)
    }

    data class PathBeginEnd(
        val begin: OverlapMatrix.Key,
        val end: OverlapMatrix.Key
    )

    fun findPathBeginEnd(
        existSpans: List<Blender.SmallInterval>,
        overlapSpans: List<Blender.SmallInterval>,
        osm: OverlapMatrix
    ): Option<PathBeginEnd> {

        data class Key(val row: Int, val column: Int)

        data class Value(
            val begin: Key,
            val grade: Grade,
            val blendTimeI: Double = existSpans[begin.row].span,
            val blendTimeJ: Double = overlapSpans[begin.column].span,
            val distSum: Double = grade.value
        ) {
            val overlapSpan: Double = blendTimeI.lerp(blendRate, blendTimeJ)
            fun extend(key: Key, grade: Grade): Value = copy(
                grade = this.grade.and(grade),
                blendTimeI = blendTimeI + existSpans[key.row].span,
                blendTimeJ = blendTimeJ + overlapSpans[key.column].span,
                distSum = distSum + (1 - grade.value)
            )
        }

        val compare = compareBy<Value>({ it.overlapSpan }, { it.grade.value }, { -it.distSum })
        val cache = IndexedCache2D<Option<Value>>(osm.rowSize, osm.columnSize)
        val random = Random(1234)
        fun dp(key: Key): Option<Value> {
            val (i, j) = key
            if (cache.contains(i, j)) return cache[i, j]!!
            val grade = osm[i, j]
            val value = when {
                osm[i, j] <= overlapThreshold -> return None
                i == 0 && j == 0 -> return Some(Value(key, grade))
                i == 0 -> dp(Key(i, j - 1)).map { it.extend(key, grade) } + Value(key, grade)
                j == 0 -> dp(Key(i - 1, j)).map { it.extend(key, grade) } + Value(key, grade)
                else -> (dp(Key(i - 1, j - 1)) + dp(Key(i, j - 1)) + dp(Key(i - 1, j))).map {
                    it.extend(Key(i, j), grade)
                }
            }.shuffled(random).maxWithOrNull(compare).toOption()
            cache[i, j] = value
            return value
        }

        val right = (0 until osm.rowSize).map { i -> Key(i, osm.columnLastIndex) }
        val bottom = (0 until osm.columnSize).map { j -> Key(osm.rowLastIndex, j) }
        val (key, value) = (right + bottom)
            .flatMap { key -> dp(key).map { key to it } }
            .shuffled(random)
            .maxWithOrNull(Comparator.comparing({ (_, value) -> value }, compare))
            ?: return None
        return Some(
            PathBeginEnd(
                OverlapMatrix.Key(value.begin.row, value.begin.column),
                OverlapMatrix.Key(key.row, key.column)
            )
        )
    }

    fun findMiddlePath(
        osm: OverlapMatrix,
        pathBeginEnd: PathBeginEnd
    ): OverlapState.Path {

        data class Key(val row: Int, val column: Int)

        data class Dir(val rowDelta: Int, val columnDelta: Int)

        operator fun Key.plus(dir: Dir): Key = Key(row + dir.rowDelta, column + dir.columnDelta)
        val up = Dir(-1, 0)
        val left = Dir(0, -1)
        val upLeft = Dir(-1, -1)
        val stop = Dir(0, 0)

        data class Value(
            val prev: Dir,
            val grade: Grade,
            val distSum: Double = (1 - grade.value)
        ) {
            fun extend(prev: Dir, grade: Grade): Value =
                copy(prev = prev, grade = this.grade.and(grade), distSum = distSum + (1 - grade.value))
        }

        val compare = compareBy<Value>({ it.grade.value }, { -it.distSum })
        val cache = IndexedCache2D<Option<Value>>(osm.rowSize, osm.columnSize)
        val random = Random(5678)
        fun dp(key: Key): Option<Value> {
            val (i, j) = key
            if (cache.contains(i, j)) return cache[i, j]!!
            val grade = osm[i, j]
            val values = when {
                osm[i, j] <= overlapThreshold -> return None
                i < pathBeginEnd.begin.row -> return None
                j < pathBeginEnd.begin.column -> return None
                i == pathBeginEnd.begin.row && j == pathBeginEnd.begin.column ->
                    return Some(Value(stop, grade))
                i == pathBeginEnd.begin.row -> dp(Key(i, j - 1)).map { it.extend(left, grade) }
                j == pathBeginEnd.begin.column -> dp(Key(i - 1, j)).map { it.extend(up, grade) }
                else -> listOf(
                    dp(Key(i - 1, j - 1)).map { it.extend(upLeft, grade) },
                    dp(Key(i, j - 1)).map { it.extend(left, grade) },
                    dp(Key(i - 1, j)).map { it.extend(up, grade) }
                ).flatten()
            }
            val value = values.shuffled(random).maxWithOrNull(compare).toOption()
            cache[i, j] = value
            return value
        }

        val path = mutableListOf<OverlapMatrix.Key>().apply {
            var k = pathBeginEnd.let { Key(it.end.row, it.end.column) }
            while (true) {
                add(OverlapMatrix.Key(k.row, k.column))
                val dir = dp(k).orThrow()
                if (dir.prev == stop) break
                k += dir.prev
            }
        }
        val grade = path.fold(Grade.TRUE) { g, k -> g.and(osm[k]) }
        return OverlapState.Path(grade, path.reversed())
    }

    fun findFront(
        osm: OverlapMatrix,
        middlePath: OverlapState.Path
    ): OverlapState.Path {
        val path = mutableListOf<OverlapMatrix.Key>().apply {
            var key = middlePath.first()
            if (key == OverlapMatrix.Key(0, 0)) return@apply
            while (true) {
                key = OverlapMatrix.Key(
                    (key.row - 1).coerceAtLeast(0),
                    (key.column - 1).coerceAtLeast(0)
                )
                if (osm[key] == Grade.FALSE) break
                add(key)
                if (key == OverlapMatrix.Key(0, 0)) return@apply
            }
        }
        val grade = path.fold(middlePath.grade) { g, k -> g.and(osm[k]) }
        return OverlapState.Path(grade, path.reversed())
    }

    fun findBack(
        osm: OverlapMatrix,
        middlePath: OverlapState.Path
    ): OverlapState.Path {
        val path = mutableListOf<OverlapMatrix.Key>().apply {
            var key = middlePath.last()
            if (key == OverlapMatrix.Key(osm.rowLastIndex, osm.columnLastIndex)) return@apply
            while (true) {
                key = OverlapMatrix.Key(
                    (key.row + 1).coerceAtMost(osm.rowLastIndex),
                    (key.column + 1).coerceAtMost(osm.columnLastIndex)
                )
                if (osm[key] == Grade.FALSE) break
                add(key)
                if (key == OverlapMatrix.Key(osm.rowLastIndex, osm.columnLastIndex)) return@apply
            }
        }
        val grade = path.fold(middlePath.grade) { g, k -> g.and(osm[k]) }
        return OverlapState.Path(grade, path)
    }
}
