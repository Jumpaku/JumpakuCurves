package jumpaku.curves.fsc.blend

class IndexedCache2D<T : Any> private constructor(
    val rowSize: Int,
    val columnSize: Int,
    private val buffer: MutableList<T?>
) : Iterable<T> {

    constructor(
        rowSize: Int,
        columnSize: Int
    ) : this(rowSize, columnSize, MutableList(rowSize * columnSize) { null })

    constructor(
        rowSize: Int,
        columnSize: Int,
        init: (i: Int, j: Int) -> T
    ) : this(rowSize, columnSize, MutableList(rowSize * columnSize) { init(it / columnSize, it % columnSize) })

    constructor(
        rowSize: Int,
        columnSize: Int,
        init: (key: Key) -> T
    ) : this(rowSize, columnSize, MutableList(rowSize * columnSize) { init(Key(it / columnSize, it % columnSize)) })


    data class Key(val row: Int, val column: Int)

    private fun index(i: Int, j: Int): Int = i * columnSize + j

    operator fun get(key: Key): T? = get(key.row, key.column)

    operator fun get(i: Int, j: Int): T? = buffer[index(i, j)]

    operator fun set(i: Int, j: Int, value: T): T? {
        val idx = index(i, j)
        val prev = buffer[idx]
        buffer[idx] = value
        return prev
    }

    operator fun set(key: Key, value: T): T? = set(key.row, key.column, value)

    fun put(i: Int, j: Int, value: T): T? = set(i, j, value)

    fun put(key: Key, value: T): T? = put(key.row, key.column, value)

    fun putAndGet(i: Int, j: Int, value: T): T {
        set(i, j, value)
        return value
    }

    fun putAndGet(key: Key, value: T): T = putAndGet(key.row, key.column, value)

    fun remove(i: Int, j: Int): T? {
        val idx = index(i, j)
        val prev = buffer[idx]
        buffer[idx] = null
        return prev
    }

    fun remove(key: Key): T? = remove(key.row, key.column)

    fun getOrElse(i: Int, j: Int, defaultValue: (i: Int, j: Int) -> T): T = get(i, j) ?: defaultValue(i, j)

    fun getOrElse(key: Key, defaultValue: (key: Key) -> T): T = get(key) ?: defaultValue(key)

    fun getOrPut(i: Int, j: Int, defaultValue: () -> T): T {
        val idx = index(i, j)
        buffer[idx] = buffer[idx] ?: defaultValue()
        return buffer[idx]!!
    }

    fun getOrPut(key: Key, defaultValue: () -> T): T = getOrPut(key.row, key.column, defaultValue)

    fun contains(i: Int, j: Int): Boolean = buffer[index(i, j)] != null

    operator fun contains(key: Key): Boolean = contains(key.row, key.column)

    override fun iterator(): Iterator<T> = sequence {
        for (value in buffer)
            if (value != null)
                yield(value)
    }.iterator()

    override fun toString(): String = List(rowSize) { i -> List(columnSize) { j -> get(i, j) } }.toString()
}