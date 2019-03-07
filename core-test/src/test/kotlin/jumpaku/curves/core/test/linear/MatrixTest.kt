package jumpaku.curves.core.test.linear

import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.linear.Matrix
import jumpaku.curves.core.linear.Vector
import jumpaku.curves.core.test.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import java.time.Duration
import kotlin.random.Random


class MatrixTest {

    val id44 = Matrix.Identity(4)
    val id33 = Matrix.Identity(3)
    val di44 = Matrix.Diagonal(listOf(-0.5, 2.0, 1.0, 3.0))
    val di33 = Matrix.Diagonal(listOf(-0.5, 2.0, 1.0))

    val data34 = listOf(
            listOf(3.0, 1.0, 2.0, 0.0),
            listOf(2.0, 4.0, 2.0, 2.0),
            listOf(1.0, -4.0, 0.0, 1.0))

    val data43 = listOf(
            listOf(2.0, 3.0, -2.0),
            listOf(1.0, 4.0, 2.0),
            listOf(2.0, 1.0, 2.0),
            listOf(1.0, 0.0, -3.0))

    fun buildSparse(data: List<List<Double>>): Map<Matrix.Sparse.Key, Double> {
        val c = mutableMapOf<Matrix.Sparse.Key, Double>()
        for (i in 0 until data.size) for (j in 0 until data[0].size) data[i][j].let { if (it != 0.0) c[Matrix.Sparse.Key(i, j)] = it }
        return c
    }
    val sp34 = Matrix.Sparse(3, 4, buildSparse(data34))
    val sp43 = Matrix.Sparse(4, 3, buildSparse(data43))
    val ar34 = Matrix.Array2D(data34)
    val ar43 = Matrix.Array2D(data43)

    @Test
    fun testTimesIdentity() {
        assertThat(id33*id33, `is`(closeTo(id33)))
        assertThat(id44*id44, `is`(closeTo(id44)))

        assertThat(id33*di33, `is`(closeTo(di33)))
        assertThat(id33*sp34, `is`(closeTo(sp34)))
        assertThat(id33*ar34, `is`(closeTo(ar34)))

        assertThat(di33*id33, `is`(closeTo(di33)))
        assertThat(sp43*id33, `is`(closeTo(sp43)))
        assertThat(ar43*id33, `is`(closeTo(ar43)))

        assertThat(id44*di44, `is`(closeTo(di44)))
        assertThat(id44*sp43, `is`(closeTo(sp43)))
        assertThat(id44*ar43, `is`(closeTo(ar43)))

        assertThat(di44*id44, `is`(closeTo(di44)))
        assertThat(sp34*id44, `is`(closeTo(sp34)))
        assertThat(ar34*id44, `is`(closeTo(ar34)))

    }

    @Test
    fun testTimesDiagonal() {
        val e33_33 = Matrix.Diagonal(listOf(0.25, 4.0, 1.0))
        assertThat(di33*di33, `is`(closeTo(e33_33)))

        val e44_44 = Matrix.Diagonal(listOf(0.25, 4.0, 1.0, 9.0))
        assertThat(di44*di44, `is`(closeTo(e44_44)))

        val e33_34 = Matrix.Array2D(listOf(
                listOf(-1.5, -0.5, -1.0, 0.0),
                listOf(4.0, 8.0, 4.0, 4.0),
                listOf(1.0, -4.0, 0.0, 1.0)))
        assertThat(di33*sp34, `is`(closeTo(e33_34)))
        assertThat(di33*ar34, `is`(closeTo(e33_34)))

        val e43_33 = Matrix.Array2D(listOf(
                listOf(-1.0, 6.0, -2.0),
                listOf(-0.5, 8.0, 2.0),
                listOf(-1.0, 2.0, 2.0),
                listOf(-0.5, 0.0, -3.0)))
        assertThat(sp43*di33, `is`(closeTo(e43_33)))
        assertThat(ar43*di33, `is`(closeTo(e43_33)))


        val e44_43 = Matrix.Array2D(listOf(
                listOf(-1.0, -1.5, 1.0),
                listOf(2.0, 8.0, 4.0),
                listOf(2.0, 1.0, 2.0),
                listOf(3.0, 0.0, -9.0)))
        assertThat(di44*sp43, `is`(closeTo(e44_43)))
        assertThat(di44*ar43, `is`(closeTo(e44_43)))

        val e34_44 = Matrix.Array2D(listOf(
                listOf(-1.5, 2.0, 2.0, 0.0),
                listOf(-1.0, 8.0, 2.0, 6.0),
                listOf(-0.5, -8.0, 0.0, 3.0)))
        assertThat(sp34*di44, `is`(closeTo(e34_44)))
        assertThat(ar34*di44, `is`(closeTo(e34_44)))
    }

    @Test
    fun testTimesSparseArray2D() {
        val e34_43 = Matrix.Array2D(listOf(
                listOf(11.0, 15.0, 0.0),
                listOf(14.0, 24.0, 2.0),
                listOf(-1.0, -13.0, -13.0)))
        assertThat(sp34*sp43, `is`(closeTo(e34_43)))
        assertThat(sp34*ar43, `is`(closeTo(e34_43)))
        assertThat(ar34*ar43, `is`(closeTo(e34_43)))
        assertThat(ar34*sp43, `is`(closeTo(e34_43)))

        val e43_34 = Matrix.Array2D(listOf(
                listOf(10.0, 22.0, 10.0, 4.0),
                listOf(13.0, 9.0, 10.0, 10.0),
                listOf(10.0, -2.0, 6.0, 4.0),
                listOf(0.0, 13.0, 2.0, -3.0)))
        assertThat(sp43*sp34, `is`(closeTo(e43_34)))
        assertThat(sp43*ar34, `is`(closeTo(e43_34)))
        assertThat(ar43*ar34, `is`(closeTo(e43_34)))
        assertThat(ar43*sp34, `is`(closeTo(e43_34)))
    }

    @Test
    fun testTranspose() {
        assertThat(id44.transpose(), `is`(closeTo(id44)))
        assertThat(di33.transpose(), `is`(closeTo(di33)))
        val e34 = Matrix.Array2D(listOf(
                listOf(3.0, 2.0, 1.0),
                listOf(1.0, 4.0, -4.0),
                listOf(2.0, 2.0, 0.0),
                listOf(0.0, 2.0, 1.0)))
        assertThat(sp34.transpose(), `is`(closeTo(e34)))
        assertThat(ar34.transpose(), `is`(closeTo(e34)))
    }

    @Test
    fun testRows() {
        val a_id44 = id44.rows()
        val e_id44 = listOf(
                Vector.Sparse(4, mapOf(0 to 1.0)),
                Vector.Sparse(4, mapOf(1 to 1.0)),
                Vector.Sparse(4, mapOf(2 to 1.0)),
                Vector.Sparse(4, mapOf(3 to 1.0)))
        assertThat(a_id44.size, `is`(e_id44.size))
        a_id44.zip(e_id44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a_di44 = di44.rows()
        val e_di44 = listOf(
                Vector.Sparse(4, mapOf(0 to -0.5)),
                Vector.Sparse(4, mapOf(1 to 2.0)),
                Vector.Sparse(4, mapOf(2 to 1.0)),
                Vector.Sparse(4, mapOf(3 to 3.0)))
        assertThat(a_di44.size, `is`(e_di44.size))
        a_di44.zip(e_di44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a_sp44 = sp34.rows()
        val e_sp44 = listOf(
                Vector.Array(listOf(3.0, 1.0, 2.0, 0.0)),
                Vector.Array(listOf(2.0, 4.0, 2.0, 2.0)),
                Vector.Array(listOf(1.0, -4.0, 0.0, 1.0)))
        assertThat(a_sp44.size, `is`(e_sp44.size))
        a_sp44.zip(e_sp44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a_ar44 = ar34.rows()
        val e_ar44 = e_sp44
        assertThat(a_ar44.size, `is`(e_ar44.size))
        a_ar44.zip(e_ar44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }
    }

    @Test
    fun testColumns() {
        val a_id44 = id44.columns()
        val e_id44 = listOf(
                Vector.Sparse(4, mapOf(0 to 1.0)),
                Vector.Sparse(4, mapOf(1 to 1.0)),
                Vector.Sparse(4, mapOf(2 to 1.0)),
                Vector.Sparse(4, mapOf(3 to 1.0)))
        assertThat(a_id44.size, `is`(e_id44.size))
        a_id44.zip(e_id44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a_di44 = di44.columns()
        val e_di44 = listOf(
                Vector.Sparse(4, mapOf(0 to -0.5)),
                Vector.Sparse(4, mapOf(1 to 2.0)),
                Vector.Sparse(4, mapOf(2 to 1.0)),
                Vector.Sparse(4, mapOf(3 to 3.0)))
        assertThat(a_di44.size, `is`(e_di44.size))
        a_di44.zip(e_di44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a_sp44 = sp34.columns()
        val e_sp44 = listOf(
                Vector.Array(listOf(3.0, 2.0, 1.0)),
                Vector.Array(listOf(1.0, 4.0, -4.0)),
                Vector.Array(listOf(2.0, 2.0, 0.0)),
                Vector.Array(listOf(0.0, 2.0, 1.0)))
        assertThat(a_sp44.size, `is`(e_sp44.size))
        a_sp44.zip(e_sp44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a_ar44 = ar34.columns()
        val e_ar44 = e_sp44
        assertThat(a_ar44.size, `is`(e_ar44.size))
        a_ar44.zip(e_ar44).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }
    }

    @Test
    fun testToDoubleArrays() {
        val a_id44 = id44.toDoubleArrays()
        val e_id44 = arrayOf(
                Vector.Sparse(4, mapOf(0 to 1.0)),
                Vector.Sparse(4, mapOf(1 to 1.0)),
                Vector.Sparse(4, mapOf(2 to 1.0)),
                Vector.Sparse(4, mapOf(3 to 1.0)))
                .map { it.toDoubleArray() }
        assertThat(a_id44.size, `is`(e_id44.size))
        a_id44.zip(e_id44).forEach { (a, e) ->
            assertThat(a.size, `is`(e.size))
            a.zip(e).forEach { (ae, ee) -> assertThat(ae, `is`(closeTo(ee))) }
        }

        val a_di44 = di44.toDoubleArrays()
        val e_di44 = arrayOf(
                Vector.Sparse(4, mapOf(0 to -0.5)),
                Vector.Sparse(4, mapOf(1 to 2.0)),
                Vector.Sparse(4, mapOf(2 to 1.0)),
                Vector.Sparse(4, mapOf(3 to 3.0)))
                .map { it.toDoubleArray() }
        assertThat(a_di44.size, `is`(e_di44.size))
        a_di44.zip(e_di44).forEach { (a, e) ->
            assertThat(a.size, `is`(e.size))
            a.zip(e).forEach { (ae, ee) -> assertThat(ae, `is`(closeTo(ee))) }
        }
        val a_sp44 = sp34.toDoubleArrays()
        val e_sp44 = arrayOf(
                Vector.Array(listOf(3.0, 1.0, 2.0, 0.0)),
                Vector.Array(listOf(2.0, 4.0, 2.0, 2.0)),
                Vector.Array(listOf(1.0, -4.0, 0.0, 1.0)))
                .map { it.toDoubleArray() }
        assertThat(a_sp44.size, `is`(e_sp44.size))
        a_sp44.zip(e_sp44).forEach { (a, e) ->
            assertThat(a.size, `is`(e.size))
            a.zip(e).forEach { (ae, ee) -> assertThat(ae, `is`(closeTo(ee))) }
        }
        val a_ar44 = ar34.toDoubleArrays()
        val e_ar44 = e_sp44
        assertThat(a_ar44.size, `is`(e_ar44.size))
        a_ar44.zip(e_ar44).forEach { (a, e) ->
            assertThat(a.size, `is`(e.size))
            a.zip(e).forEach { (ae, ee) -> assertThat(ae, `is`(closeTo(ee))) }
        }
    }

    @Test
    fun testToJsonString() {
        val a_id44 = id44.toJsonString().parseJson().tryMap { Matrix.fromJson(it) }.orThrow()
        assertThat(a_id44, `is`(closeTo(id44)))
        val a_di44 = di44.toJsonString().parseJson().tryMap { Matrix.fromJson(it) }.orThrow()
        assertThat(a_di44, `is`(closeTo(di44)))
        val a_sp34 = sp34.toJsonString().parseJson().tryMap { Matrix.fromJson(it) }.orThrow()
        assertThat(a_sp34, `is`(closeTo(sp34)))
        val a_ar34 = ar34.toJsonString().parseJson().tryMap { Matrix.fromJson(it) }.orThrow()
        assertThat(a_ar34, `is`(closeTo(ar34)))
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

    @Test
    fun testPerformance() {
        val r = 2000
        val c = 200
        val n = 8000
        val data = mutableMapOf<Matrix.Sparse.Key, Double>()
        val rand = Random(283)
        repeat(n) {
            var key: Matrix.Sparse.Key
            do {
                val i = rand.nextInt(r)
                val j = rand.nextInt(c)
                key = Matrix.Sparse.Key(i, j)
            } while (key in data)
            data[key] = rand.nextDouble(-1e4, 1e4)
        }

        val s = Matrix.Sparse(r, c, data)
        repeat(20) { s.transpose() * s }
        val start = System.nanoTime()
        assertTimeoutPreemptively(Duration.ofMillis(2000)) {
            repeat(20) { s.transpose() * s }
            println("    ${(System.nanoTime()-start)*1e-9}")
        }
    }
}
