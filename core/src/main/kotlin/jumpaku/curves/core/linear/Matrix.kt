package jumpaku.curves.core.linear

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.json.jsonMap
import jumpaku.curves.core.json.map
import jumpaku.curves.core.util.sum
import org.apache.commons.math3.linear.OpenMapRealMatrix
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.random.Random
import kotlin.system.measureNanoTime


sealed class Matrix(val rowSize: Int, val columnSize: Int): ToJson {

    operator fun times(other: Matrix): Matrix {
        require(columnSize == other.rowSize) {
                "Dimension(($rowSize,$columnSize) x (${other.rowSize},${other.columnSize})) mismatched"
        }
        return timesImpl(this, other)
    }

    fun transpose(): Matrix = when(this) {
        is Identity, is Diagonal -> this
        is Sparse -> Sparse(columnSize, rowSize, data.mapKeys { (key, _) -> key.swap() })
        is Array2D -> Array2D(Array(columnSize) { j -> DoubleArray(rowSize) { i -> get(i, j) } })
    }

    fun rows(): List<Vector> = when(this) {
        is Identity, is Diagonal -> (0 until rowSize).map { i -> Vector.Sparse(columnSize, mapOf(i to get(i, i))) }
        is Array2D -> (0 until rowSize).map { i -> Vector.Array((0 until columnSize).map { j -> get(i, j) }) }
        is Sparse -> {
            val tmp = List(rowSize) { LinkedList<Int>() }
            data.keys.forEach { (i, j) -> tmp[i].add(j) }
            tmp.mapIndexed { i, l -> Vector.Sparse(columnSize, l.map { j -> j to get(i, j) }.toMap()) }
        }
    }

    fun columns(): List<Vector> = when(this) {
        is Identity, is Diagonal -> (0 until columnSize).map { i -> Vector.Sparse(rowSize, mapOf(i to get(i, i))) }
        is Array2D -> (0 until columnSize).map { j -> Vector.Array((0 until rowSize).map { i -> get(i, j) }) }
        is Sparse -> {
            val tmp = List(columnSize) { LinkedList<Int>() }
            data.keys.forEach { (i, j) -> tmp[j].add(i) }
            tmp.mapIndexed { j, l -> Vector.Sparse(rowSize, l.map { i -> i to get(i, j) }.toMap()) }
        }
    }

    abstract operator fun get(i: Int, j: Int): Double

    fun toDoubleArrays(): Array<DoubleArray> = Array(rowSize) { i -> DoubleArray(columnSize) { j -> get(i, j) } }

    abstract override fun toJson(): JsonElement

    override fun toString(): String = toDoubleArrays().map { it.toList() }.toString()//toJsonString()

    class Identity(dimension: Int): Matrix(dimension, dimension) {

        override fun get(i: Int, j: Int): Double = if (i == j) 1.0 else 0.0

        override fun toJson(): JsonElement = jsonObject("type" to "Identity", "size" to rowSize)
    }

    class Diagonal(data: List<Double>): Matrix(data.size, data.size) {

        constructor(data: DoubleArray): this(data.toList())

        val data: List<Double> = data.toList()

        override fun toJson(): JsonElement = jsonObject(
                "type" to "Diagonal".toJson(),
                "data" to jsonArray(data.map { it.toJson() }))

        override fun get(i: Int, j: Int): Double = if (i == j) data[i] else 0.0
    }

    class Sparse(rowSize: Int, columnSize: Int, data: Map<Key, Double>): Matrix(rowSize, columnSize) {

        data class Key(val row: Int, val column: Int) {

            fun swap(): Key = Key(column, row)
        }

        val data: Map<Key, Double> = data.toMap()

        init {
            require(data.keys.all { (i, j) -> i in 0 until rowSize && j in 0 until columnSize }) { "key out of range" }
        }

        override fun get(i: Int, j: Int): Double = data[Key(i, j)] ?: 0.0

        override fun toJson(): JsonElement = jsonObject(
                "type" to "Sparse".toJson(),
                "rowSize" to rowSize.toJson(),
                "columnSize" to columnSize.toJson(),
                "data" to jsonMap(data.map { (key, value) ->
                    jsonObject("row" to key.row, "column" to key.column) to value.toJson()
                }.toMap()))
    }

    class Array2D(rowSize: Int, columnSize: Int, f: (Int, Int) -> Double): Matrix(rowSize, columnSize) {

        val data: List<List<Double>> = List(rowSize) { i -> List(columnSize) { j -> f(i, j) } }

        constructor(data: Array<DoubleArray>): this(data.size, data[0].size, { i, j -> data[i][j] })

        constructor(data: List<List<Double>>): this(data.size, data[0].size, { i, j -> data[i][j] })

        override fun get(i: Int, j: Int): Double = data[i][j]

        override fun toJson(): JsonElement = jsonObject(
                "type" to "Array2D".toJson(),
                "data" to jsonArray(data.map { jsonArray(it.map { it.toJson() }) } ))
    }

    companion object {

        fun fromJson(json: JsonElement): Matrix = when(json["type"].string) {
            "Identity" -> Identity(json["size"].int)
            "Diagonal" -> Diagonal(json["data"].array.map { it.double })
            "Sparse" -> Sparse(
                    json["rowSize"].int,
                    json["columnSize"].int,
                    json["data"].map.map { (key, value) ->
                        Sparse.Key(key["row"].int, key["column"].int) to value.double
                    }.toMap())
            "Array2D" -> Array2D(json["data"].array.map { it.array.map { it.double } })
            else -> error("invalid matrix type")
        }
    }
}

private fun timesImpl(a: Matrix, b: Matrix): Matrix = when {
    a is Matrix.Identity -> b
    b is Matrix.Identity -> a
    a is Matrix.Diagonal && b is Matrix.Diagonal -> Matrix.Diagonal(
            a.data.zip(b.data, Double::times))
    a is Matrix.Diagonal && b is Matrix.Sparse -> Matrix.Sparse(
            b.rowSize, b.columnSize, b.data.map { (ij, m_kj) -> ij to m_kj * a.data[ij.row] }.toMap())
    a is Matrix.Diagonal && b is Matrix.Array2D -> Matrix.Array2D(
            b.data.mapIndexed { i, r -> r.map { e -> e * a.data[i] }.toDoubleArray() }.toTypedArray())
    a is Matrix.Sparse && b is Matrix.Diagonal -> Matrix.Sparse(
            a.rowSize, a.columnSize, a.data.mapValues { (ij, m_ij) -> m_ij * b.data[ij.column] })
    a is Matrix.Array2D && b is Matrix.Diagonal -> Matrix.Array2D(
            a.data.map { r -> r.mapIndexed { j, e -> e * b.data[j] }.toDoubleArray() }.toTypedArray())
    a is Matrix.Sparse && b is Matrix.Sparse -> {
        val bKeysOfColumn = b.data.keys.groupBy { (_, j) -> j }.mapValues { it.value.toSet() }
        val c = mutableMapOf<Matrix.Sparse.Key, MutableList<Double>>()
        for ((i, k) in a.data.keys) {
            for ((j, bKey) in bKeysOfColumn) {
                if (Matrix.Sparse.Key(k, j) in bKey) {
                    val key = Matrix.Sparse.Key(i, j)
                    c.compute(key) { _, arr -> (arr ?: ArrayList(a.rowSize)).apply { add(a[i, k] * b[k, j]) } }
                }
            }
        }
        Matrix.Sparse(a.rowSize, b.columnSize, c.mapValues { (_, l) -> sum(l) })
    }
    a is Matrix.Sparse && b is Matrix.Array2D -> {
        val c = mutableMapOf<Matrix.Sparse.Key, MutableList<Double>>()
        for ((i, k) in a.data.keys) {
            for (j in 0 until b.columnSize) {
                val key = Matrix.Sparse.Key(i, j)
                c.compute(key) { _, l -> (l ?: ArrayList(a.columnSize)).apply { add(a[i, k]*b[k, j]) } }
            }
        }
        Matrix.Array2D(a.rowSize, b.columnSize) { i, j ->
            if (Matrix.Sparse.Key(i, j) in c) sum(c[Matrix.Sparse.Key(i, j)]!!) else 0.0
        }
    }
    a is Matrix.Array2D && b is Matrix.Sparse -> {
        val c = mutableMapOf<Matrix.Sparse.Key, MutableList<Double>>()
        for (i in 0 until a.rowSize) {
            for ((k, j) in b.data.keys) {
                val key = Matrix.Sparse.Key(i, j)
                c.compute(key) { _, l -> (l ?: ArrayList(a.columnSize)).apply { add(a[i, k]*b[k, j]) } }
            }
        }
        Matrix.Array2D(a.rowSize, b.columnSize) { i, j ->
            if (Matrix.Sparse.Key(i, j) in c) sum(c[Matrix.Sparse.Key(i, j)]!!) else 0.0
        }
    }
    else -> {
        val rows = a.rows()
        val columns = b.columns()
        Matrix.Array2D(a.rowSize, b.columnSize) { i, j -> rows[i].dot(columns[j]) }
    }
}



fun randomSparse(rowSize: Int, columnSize: Int, nElements: Int, seed: Int): Matrix.Sparse {
    val data = mutableMapOf<Matrix.Sparse.Key, Double>()
    val r = Random(seed)
    for (n in 0 until nElements) {
        var key: Matrix.Sparse.Key
        do {
            val i = r.nextInt(rowSize)
            val j = r.nextInt(columnSize)
            key = Matrix.Sparse.Key(i, j)
        } while (key in data)
        data[key] = r.nextDouble(-1e5, 1e5)
    }
    return Matrix.Sparse(rowSize, columnSize, data)
}

fun main() {
    System.out.printf("%5s | %7s | %7s | %7s |\n", "row", "time_cc", "time_ss", "ss/cc")
    for (i in 1..20) {
        val rs = i * 200
        val cs = rs / 10

        val s = randomSparse(rs, cs, rs * 4, 283)
        val c = OpenMapRealMatrix(rs, cs).apply { s.data.forEach { (i, j), v -> setEntry(i, j, v) } }

        repeat(10) {
            s.transpose() * s
            c.transpose().multiply(c)
        }
        val time_cc = measureNanoTime {
            repeat(10) { c.transpose().multiply(c) }
        }
        val time_ss = measureNanoTime {
            repeat(10) { s.transpose() * s }
        }

        System.out.printf("%5d | %2.5f | %2.5f | %2.5f |\n", rs, time_cc * 1e-10, time_ss * 1e-10, time_ss * 1e-10 / (time_cc * 1e-10))
    }
}