package jumpaku.curves.core.test.util

import jumpaku.curves.core.util.failure
import jumpaku.curves.core.util.flatten
import jumpaku.curves.core.util.result
import jumpaku.curves.core.util.success
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Test

class ResultTest {

    val success = success(4)
    val failure = failure<Int>(IllegalStateException("NG"))

    @Test
    fun testValue() {
        println("Value")
        assertThat(success.value().orNull()!!, `is`(4))
        assertThat(failure.value().isEmpty, `is`(true))
    }

    @Test
    fun testIsSuccess() {
        println("IsSuccess")
        assertThat(success.isSuccess, `is`(true))
        assertThat(failure.isSuccess, `is`(false))
    }

    @Test
    fun testIsFailure() {
        println("IsFailure")
        assertThat(success.isFailure, `is`(false))
        assertThat(failure.isFailure, `is`(true))
    }

    @Test
    fun testError() {
        println("Error")
        assertThat(success.error().isEmpty, `is`(true))
        assertThat(failure.error().orNull()!!, `is`(instanceOf(IllegalStateException::class.java)))
    }

    @Test
    fun testResult() {
        println("Result")
        val s = result { 4 }
        assertThat(s.value().orNull()!!, `is`(4))
        assertThat(s.error().isEmpty, `is`(true))
        val f = result { throw Exception() }
        assertThat(f.value().isEmpty, `is`(true))
        assertThat(f.error().orNull()!!, `is`(instanceOf(Exception::class.java)))
    }

    @Test
    fun testTryMap() {
        println("TryMap")

        val ss = success.tryMap { it.toString() }
        assertThat(ss.value().orNull()!!, `is`("4"))
        assertThat(ss.error().isEmpty, `is`(true))

        val sf = success.tryMap { throw Exception() }
        assertThat(sf.value().isEmpty, `is`(true))
        assertThat(sf.error().orNull()!!, `is`(instanceOf(Exception::class.java)))

        val fs = failure.tryMap { it.toString() }
        assertThat(fs.value().isEmpty, `is`(true))
        assertThat(fs.error().orNull()!!, `is`(instanceOf(IllegalStateException::class.java)))

        val ff = failure.tryMap { throw Exception() }
        assertThat(ff.value().isEmpty, `is`(true))
        assertThat(ff.error().orNull()!!, `is`(instanceOf(IllegalStateException::class.java)))
    }

    @Test
    fun testTryFlatMap() {
        println("TryFlatMap")

        val ss = success.tryFlatMap { result { it.toString() } }
        assertThat(ss.value().orNull()!!, `is`("4"))
        assertThat(ss.error().isEmpty, `is`(true))

        val sf = success.tryFlatMap { result { throw Exception() } }
        assertThat(sf.value().isEmpty, `is`(true))
        assertThat(sf.error().orNull()!!, `is`(instanceOf(Exception::class.java)))

        val fs = failure.tryFlatMap { result { it.toString() } }
        assertThat(fs.value().isEmpty, `is`(true))
        assertThat(fs.error().orNull()!!, `is`(instanceOf(IllegalStateException::class.java)))

        val ff = failure.tryFlatMap { result { throw Exception() } }
        assertThat(ff.value().isEmpty, `is`(true))
        assertThat(ff.error().orNull()!!, `is`(instanceOf(IllegalStateException::class.java)))
    }

    @Test
    fun testFlatten() {
        println("Flatten")

        val ss = result { success }.flatten()
        assertThat(ss.value().orNull()!!, `is`(4))
        assertThat(ss.error().isEmpty, `is`(true))

        val sf = result { failure }.flatten()
        assertThat(sf.value().isEmpty, `is`(true))
        assertThat(sf.error().orNull()!!, `is`(instanceOf(IllegalStateException::class.java)))

        val fs = result { success }.tryMap {
            throw Exception()
            @Suppress("UNREACHABLE_CODE") it
        }.flatten()
        assertThat(fs.value().isEmpty, `is`(true))
        assertThat(fs.error().orNull()!!, `is`(instanceOf(Exception::class.java)))

        val ff = result { failure }.tryMap {
            throw Exception()
            @Suppress("UNREACHABLE_CODE") it
        }.flatten()
        assertThat(ff.value().isEmpty, `is`(true))
        assertThat(ff.error().orNull()!!, `is`(instanceOf(Exception::class.java)))
    }

    @Test
    fun testTryRecover() {
        println("TryRecover")

        val ss = success.tryRecover { 5 }
        assertThat(ss.value().orNull()!!, `is`(4))
        assertThat(ss.error().isEmpty, `is`(true))

        val sf = success.tryRecover { throw Exception() }
        assertThat(sf.value().orNull()!!, `is`(4))
        assertThat(sf.error().isEmpty, `is`(true))

        val fs = failure.tryRecover { 5 }
        assertThat(fs.value().orNull()!!, `is`(5))
        assertThat(fs.error().isEmpty, `is`(true))

        val ff = failure.tryRecover { throw Exception() }
        assertThat(ff.value().isEmpty, `is`(true))
        assertThat(ff.error().orNull()!!, `is`(instanceOf(Exception::class.java)))
    }

    @Test
    fun testTryMapFailure() {
        println("TryMapFailure")

        val ss = success.tryMapFailure { Exception() }
        assertThat(ss.value().orNull()!!, `is`(4))
        assertThat(ss.error().isEmpty, `is`(true))

        val sf = success.tryMapFailure { throw Exception() }
        assertThat(sf.value().orNull()!!, `is`(4))
        assertThat(sf.error().isEmpty, `is`(true))

        val fs = failure.tryMapFailure { Exception() }
        assertThat(fs.value().isEmpty, `is`(true))
        assertThat(fs.error().orNull()!!, `is`(instanceOf(Exception::class.java)))

        val ff = failure.tryMapFailure { throw Exception() }
        assertThat(ff.value().isEmpty, `is`(true))
        assertThat(ff.error().orNull()!!, `is`(instanceOf(Exception::class.java)))
    }

    @Test
    fun testOnSuccess() {
        println("OnSuccess")
        val x0 = arrayOf(1, 2)
        success.onSuccess { x0[0] = 3 }
        assertThat(x0[0], `is`(3))
        assertThat(x0[1], `is`(2))

        val x1 = arrayOf(1, 2)
        failure.onSuccess { x1[0] = 3 }
        assertThat(x1[0], `is`(1))
        assertThat(x1[1], `is`(2))
    }

    @Test
    fun testOnFailure() {
        println("OnFailure")
        val x0 = arrayOf(1, 2)
        success.onFailure { x0[0] = 3 }
        assertThat(x0[0], `is`(1))
        assertThat(x0[1], `is`(2))

        val x1 = arrayOf(1, 2)
        failure.onFailure { x1[0] = 3 }
        assertThat(x1[0], `is`(3))
        assertThat(x1[1], `is`(2))
    }
}