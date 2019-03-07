package jumpaku.curves.core.test.linear

import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.linear.Matrix
import jumpaku.curves.core.linear.Vector
import jumpaku.curves.core.linear.times
import jumpaku.curves.core.test.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class VectorTest {

    val dataX = listOf(-1.0, 0.0, 2.0, 0.0, -3.0, 0.0, 4.0)
    val dataY = listOf(3.0, -2.0, 1.0, 5.0, 3.0, -4.0, 0.0)
    val sX = Vector.Sparse(7, dataX.mapIndexedNotNull { i, v -> v.takeIf { it != 0.0 }?.let { i to v } }.toMap())
    val sY = Vector.Sparse(7, dataY.mapIndexedNotNull { i, v -> v.takeIf { it != 0.0 }?.let { i to v } }.toMap())
    val aX = Vector.Array(dataX)
    val aY = Vector.Array(dataY)

    @Test
    fun testMap() {
        fun f(v: Double): Double = 5.0
        val e = Vector.Array(List(7) { 5.0 })
        assertThat(sX.map(::f), `is`(closeTo(e)))
        assertThat(aX.map(::f), `is`(closeTo(e)))
    }

    @Test
    fun testMapIndexed() {
        fun f(i: Int, v: Double): Double = i.toDouble()
        val e = Vector.Array(List(7) { it.toDouble() })
        assertThat(sX.mapIndexed(::f), `is`(closeTo(e)))
        assertThat(aX.mapIndexed(::f), `is`(closeTo(e)))
    }

    @Test
    fun testTimes() {
        val c = -3.0
        val e = Vector.Array(listOf(3.0, 0.0, -6.0, 0.0, 9.0, 0.0, -12.0))
        assertThat(c*sX, `is`(closeTo(e)))
        assertThat(c*aX, `is`(closeTo(e)))
        assertThat(sX*c, `is`(closeTo(e)))
        assertThat(aX*c, `is`(closeTo(e)))
    }

    @Test
    fun testDiv() {
        val c = -2.0
        val e = Vector.Array(listOf(0.5, 0.0, -1.0, 0.0, 1.5, 0.0, -2.0))
        assertThat((sX/c).orThrow(), `is`(closeTo(e)))
        assertThat((aX/c).orThrow(), `is`(closeTo(e)))
    }

    @Test
    fun testUnaryPlus() {
        assertThat(+sX, `is`(closeTo(sX)))
        assertThat(+aX, `is`(closeTo(aX)))
    }

    @Test
    fun testUnaryMinus() {
        assertThat(-sX, `is`(closeTo(-1.0*sX)))
        assertThat(-aX, `is`(closeTo(-1.0*aX)))
    }

    @Test
    fun testPlus() {
        val e = Vector.Array(listOf(2.0, -2.0, 3.0, 5.0, 0.0, -4.0, 4.0))
        assertThat(sX + sY, `is`(closeTo(e)))
        assertThat(aX + sY, `is`(closeTo(e)))
        assertThat(sX + aY, `is`(closeTo(e)))
        assertThat(aX + aY, `is`(closeTo(e)))
    }

    @Test
    fun testMinus() {
        val e = Vector.Array(listOf(-4.0, 2.0, 1.0, -5.0, -6.0, 4.0, 4.0))
        assertThat(sX - sY, `is`(closeTo(e)))
        assertThat(aX - sY, `is`(closeTo(e)))
        assertThat(sX - aY, `is`(closeTo(e)))
        assertThat(aX - aY, `is`(closeTo(e)))
    }

    @Test
    fun testDot() {
        val e = -10.0
        assertThat(sX.dot(sY), `is`(closeTo(e)))
        assertThat(aX.dot(sY), `is`(closeTo(e)))
        assertThat(sX.dot(aY), `is`(closeTo(e)))
        assertThat(aX.dot(aY), `is`(closeTo(e)))
    }

    @Test
    fun testSquare() {
        val e = 30.0
        assertThat(sX.square(), `is`(closeTo(e)))
        assertThat(aX.square(), `is`(closeTo(e)))
    }

    @Test
    fun testNorm() {
        val e = sqrt(30.0)
        assertThat(sX.norm(), `is`(closeTo(e)))
        assertThat(aX.norm(), `is`(closeTo(e)))
    }

    @Test
    fun testDistSquare() {
        val e = 114.0
        assertThat(sX.distSquare(sY), `is`(closeTo(e)))
        assertThat(aX.distSquare(sY), `is`(closeTo(e)))
        assertThat(sX.distSquare(aY), `is`(closeTo(e)))
        assertThat(aX.distSquare(aY), `is`(closeTo(e)))
    }

    @Test
    fun testDist() {
        val dataX = listOf(-1.0, 0.0, 2.0, 0.0, -3.0, 0.0, 4.0)
        val dataY = listOf(3.0, -2.0, 1.0, 5.0, 3.0, -4.0, 0.0)
        val e = sqrt(114.0)
        assertThat(sX.dist(sY), `is`(closeTo(e)))
        assertThat(aX.dist(sY), `is`(closeTo(e)))
        assertThat(sX.dist(aY), `is`(closeTo(e)))
        assertThat(aX.dist(aY), `is`(closeTo(e)))
    }

    @Test
    fun testNormalize() {
        val e = Vector.Array(listOf(-1.0/sqrt(30.0), 0.0, 2.0/sqrt(30.0), 0.0, -3.0/sqrt(30.0), 0.0, 4.0/sqrt(30.0)))
        assertThat(sX.normalize().orThrow(), `is`(closeTo(e)))
        assertThat(aX.normalize().orThrow(), `is`(closeTo(e)))
    }

    @Test
    fun testAsRow() {
        val e = Matrix.Array2D(listOf(dataX))
        assertThat(sX.asRow(), `is`(closeTo(e)))
        assertThat(aX.asRow(), `is`(closeTo(e)))
    }

    @Test
    fun testAsColumn() {
        val e = Matrix.Array2D(dataX.map(::listOf))
        assertThat(sX.asColumn(), `is`(closeTo(e)))
        assertThat(aX.asColumn(), `is`(closeTo(e)))
    }

    @Test
    fun testToDoubleArray() {
        val e = doubleArrayOf(-1.0, 0.0, 2.0, 0.0, -3.0, 0.0, 4.0)
        val a_sX = sX.toDoubleArray()
        assertThat(a_sX[0], `is`(closeTo(e[0])))
        assertThat(a_sX[1], `is`(closeTo(e[1])))
        assertThat(a_sX[2], `is`(closeTo(e[2])))
        assertThat(a_sX[3], `is`(closeTo(e[3])))
        assertThat(a_sX[4], `is`(closeTo(e[4])))
        assertThat(a_sX[5], `is`(closeTo(e[5])))
        assertThat(a_sX[6], `is`(closeTo(e[6])))
        val a_aX = aX.toDoubleArray()
        assertThat(a_aX[0], `is`(closeTo(e[0])))
        assertThat(a_aX[1], `is`(closeTo(e[1])))
        assertThat(a_aX[2], `is`(closeTo(e[2])))
        assertThat(a_aX[3], `is`(closeTo(e[3])))
        assertThat(a_aX[4], `is`(closeTo(e[4])))
        assertThat(a_aX[5], `is`(closeTo(e[5])))
        assertThat(a_aX[6], `is`(closeTo(e[6])))
    }

    @Test
    fun testToString() {
        val a_sX = sX.toString().parseJson().tryMap { Vector.fromJson(it) }.orThrow()
        assertThat(a_sX, `is`(closeTo(sX)))
        val a_aX = aX.toString().parseJson().tryMap { Vector.fromJson(it) }.orThrow()
        assertThat(a_aX, `is`(closeTo(aX)))
    }

}