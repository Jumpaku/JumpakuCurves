package jumpaku.curves.core.linear

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.json.jsonMap
import jumpaku.curves.core.json.map
import jumpaku.curves.core.util.Option
import jumpaku.curves.core.util.optionWhen
import jumpaku.curves.core.util.sum
import jumpaku.curves.core.util.toOption
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


sealed class Matrix(val rowSize: Int, val columnSize: Int): ToJson {

    operator fun times(other: Matrix): Matrix = multiply(this, other)

    fun transpose(): Matrix = when(this) {
        is Identity, is Diagonal -> this
        is Sparse -> Sparse(rowSize, columnSize, data.mapKeys { (key, _) -> key.swap() }.toMap())
        is Array2D -> Array2D(Array(columnSize) { j -> DoubleArray(rowSize) { i -> get(i, j) } })
    }

    fun rows(): List<Vector> = (0 until rowSize).map { i -> Vector((0 until columnSize).map { j -> get(i, j) }) }

    fun columns(): List<Vector> = (0 until columnSize).map { j -> Vector((0 until rowSize).map { i -> get(i, j) }) }

    abstract operator fun get(i: Int, j: Int): Double

    fun toDoubleArrays(): Array<DoubleArray> = Array(rowSize) { i -> DoubleArray(columnSize) { j -> get(i, j) } }

    abstract override fun toJson(): JsonElement

    override fun toString(): String = toJsonString()

    class Identity(dimension: Int): Matrix(dimension, dimension) {

        override fun get(i: Int, j: Int): Double = if (i == j) 1.0 else 0.0

        override fun toJson(): JsonElement = jsonObject("type" to "Identity", "dimension" to rowSize)
    }

    class Diagonal(data: List<Double>): Matrix(data.size, data.size) {

        constructor(data: DoubleArray): this(data.toList())

        val data: List<Double> = data.toList()

        override fun toJson(): JsonElement = jsonObject(
                "type" to "Diagonal".toJson(),
                "data" to jsonArray(data.map { it.toJson() }))

        override fun get(i: Int, j: Int): Double = if (i == j) data[i] else 0.0
    }

    class Sparse(rowSize: Int, columnSize: Int, f: (Key) -> Option<Double>): Matrix(rowSize, columnSize) {

        data class Key(val row: Int, val column: Int) {

            fun swap(): Key = Key(column, row)
        }

        constructor(rowSize: Int, columnSize: Int, data: Map<Key, Double>)
                : this(rowSize, columnSize, { data[it].toOption() })

        val data: Map<Key, Double> =  (0 until rowSize).flatMap { i ->
            (0 until columnSize).flatMap { j ->
                val key = Key(i, j)
                f(key).map { key to it }
            }
        }.toMap()

        override fun get(i: Int, j: Int): Double = if (Key(i, j) in data) data[Key(i, j)]!! else 0.0

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
                "type" to "Sparse".toJson(),
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

private fun multiply(a: Matrix, b: Matrix): Matrix = when {
    a is Matrix.Identity -> b
    b is Matrix.Identity -> a
    a is Matrix.Diagonal && b is Matrix.Diagonal -> Matrix.Diagonal(
            a.data.zip(b.data, Double::times))
    a is Matrix.Diagonal && b is Matrix.Sparse -> Matrix.Sparse(
            b.rowSize, b.columnSize, b.data.map { (ij, m_kj) -> ij to m_kj * a.data[ij.row] }.toMap())
    a is Matrix.Diagonal && b is Matrix.Array2D -> Matrix.Array2D(
            b.data.mapIndexed { i, r -> r.mapIndexed { j, e -> e * a.data[i] }.toDoubleArray() }.toTypedArray())
    a is Matrix.Sparse && b is Matrix.Diagonal -> Matrix.Sparse(
            a.rowSize, a.columnSize, a.data.map { (ij, m_ij) -> ij to m_ij * b.data[ij.column] }.toMap())
    a is Matrix.Array2D && b is Matrix.Diagonal -> Matrix.Array2D(
            a.data.mapIndexed { i, r -> r.mapIndexed { j, e -> e * b.data[j] }.toDoubleArray() }.toTypedArray())
    a is Matrix.Sparse && b is Matrix.Sparse -> {
        val bKeysOfColumn = b.data.keys.groupBy { (_, j) -> j }.mapValues { it.value.toSet() }
        val c = mutableMapOf<Matrix.Sparse.Key, LinkedList<Double>>()
        for ((i, k) in a.data.keys) {
            for ((j, bKey) in bKeysOfColumn) {
                if (Matrix.Sparse.Key(k, j) in bKey) {
                    val value_ij = a[i, k] * b[k, j]
                    val ij = Matrix.Sparse.Key(i, j)
                    if (ij in c) c[ij]!!.add(value_ij) else c[ij] = LinkedList(listOf(value_ij))
                }
            }
        }
        Matrix.Sparse(a.rowSize, b.columnSize, c.mapValues { (_, l) -> sum(Vector(l)) })
    }
    a is Matrix.Sparse && b is Matrix.Array2D -> {
        val c = mutableMapOf<Matrix.Sparse.Key, LinkedList<Double>>()
        for ((i, k) in a.data.keys) {
            for (j in 0 until b.columnSize) {
                val value_ij = a[i, k]*b[k, j]
                val ij = Matrix.Sparse.Key(i, j)
                if (ij in c) c[ij]!!.add(value_ij) else c[ij] = LinkedList(listOf(value_ij))
            }
        }
        Matrix.Array2D(a.rowSize, b.columnSize) { i, j ->
            if (Matrix.Sparse.Key(i, j) in c) sum(c[Matrix.Sparse.Key(i, j)]!!) else 0.0
        }
    }
    a is Matrix.Array2D && b is Matrix.Sparse -> {
        val c = mutableMapOf<Matrix.Sparse.Key, LinkedList<Double>>()
        for (i in 0 until a.rowSize) {
            for ((k, j) in b.data.keys) {
                val value_ij = a[i, k]*b[k, j]
                val ij = Matrix.Sparse.Key(i, j)
                if (ij in c) c[ij]!!.add(value_ij) else c[ij] = LinkedList(listOf(value_ij))
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