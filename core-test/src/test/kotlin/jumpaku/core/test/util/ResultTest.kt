package jumpaku.core.test.util

import jumpaku.core.util.failure
import jumpaku.core.util.flatten
import jumpaku.core.util.result
import jumpaku.core.util.success
import org.amshove.kluent.*
import org.junit.Test

class ResultTest {

    val success = success(4)
    val failure = failure<Int>(IllegalStateException("NG"))

    @Test
    fun testValue() {
        println("Value")
        success.value().orNull()!!.shouldEqualTo(4)
        failure.value().isEmpty.shouldBeTrue()
    }

    @Test
    fun testIsSuccess() {
        println("IsSuccess")
        success.isSuccess.shouldBeTrue()
        failure.isSuccess.shouldBeFalse()
    }

    @Test
    fun testIsFailure() {
        println("IsFailure")
        success.isFailure.shouldBeFalse()
        failure.isFailure.shouldBeTrue()
    }

    @Test
    fun testError() {
        println("Error")
        success.error().isEmpty.shouldBeTrue()
        failure.error().orNull()!!.shouldBeInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun testResult() {
        println("Result")
        val s = result { 4 }
        s.value().orNull()!!.shouldEqualTo(4)
        s.error().isEmpty.shouldBeTrue()
        val f = result { throw Exception() }
        f.value().isEmpty.shouldBeTrue()
        f.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)
    }

    @Test
    fun testTryMap() {
        println("TryMap")

        val ss = success.tryMap { it.toString() }
        ss.value().orNull()!!.shouldBeEqualTo("4")
        ss.error().isEmpty.shouldBeTrue()

        val sf = success.tryMap { throw Exception() }
        sf.value().isEmpty.shouldBeTrue()
        sf.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)

        val fs = failure.tryMap { it.toString() }
        fs.value().isEmpty.shouldBeTrue()
        fs.error().orNull()!!.shouldBeInstanceOf(IllegalStateException::class.java)

        val ff = failure.tryMap { throw Exception() }
        ff.value().isEmpty.shouldBeTrue()
        ff.error().orNull()!!.shouldBeInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun testTryFlatMap() {
        println("TryFlatMap")

        val ss = success.tryFlatMap { result { it.toString() } }
        ss.value().orNull()!!.shouldBeEqualTo("4")
        ss.error().isEmpty.shouldBeTrue()

        val sf = success.tryFlatMap { result { throw Exception() } }
        sf.value().isEmpty.shouldBeTrue()
        sf.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)

        val fs = failure.tryFlatMap { result { it.toString() } }
        fs.value().isEmpty.shouldBeTrue()
        fs.error().orNull()!!.shouldBeInstanceOf(IllegalStateException::class.java)

        val ff = failure.tryFlatMap { result { throw Exception() } }
        ff.value().isEmpty.shouldBeTrue()
        ff.error().orNull()!!.shouldBeInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun testFlatten() {
        println("Flatten")

        val ss = result { success }.flatten()
        ss.value().orNull()!!.shouldEqualTo(4)
        ss.error().isEmpty.shouldBeTrue()

        val sf = result { failure }.flatten()
        sf.value().isEmpty.shouldBeTrue()
        sf.error().orNull()!!.shouldBeInstanceOf(IllegalStateException::class.java)

        val fs = result { success }.tryMap {
            throw Exception()
            @Suppress("UNREACHABLE_CODE") it
        }.flatten()
        fs.value().isEmpty.shouldBeTrue()
        fs.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)

        val ff = result { failure }.tryMap {
            throw Exception()
            @Suppress("UNREACHABLE_CODE") it
        }.flatten()
        ff.value().isEmpty.shouldBeTrue()
        ff.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)
    }

    @Test
    fun testTryRecover() {
        println("TryRecover")

        val ss = success.tryRecover { 5 }
        ss.value().orNull()!!.shouldEqualTo(4)
        ss.error().isEmpty.shouldBeTrue()

        val sf = success.tryRecover { throw Exception() }
        sf.value().orNull()!!.shouldEqualTo(4)
        sf.error().isEmpty.shouldBeTrue()

        val fs = failure.tryRecover { 5 }
        fs.value().orNull()!!.shouldEqualTo(5)
        fs.error().isEmpty.shouldBeTrue()

        val ff = failure.tryRecover { throw Exception() }
        ff.value().isEmpty.shouldBeTrue()
        ff.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)
    }

    @Test
    fun testTryMapFailure() {
        println("TryMapFailure")

        val ss = success.tryMapFailure { Exception() }
        ss.value().orNull()!!.shouldEqualTo(4)
        ss.error().isEmpty.shouldBeTrue()

        val sf = success.tryMapFailure { throw Exception() }
        sf.value().orNull()!!.shouldEqualTo(4)
        sf.error().isEmpty.shouldBeTrue()

        val fs = failure.tryMapFailure { Exception() }
        fs.value().isEmpty.shouldBeTrue()
        fs.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)

        val ff = failure.tryMapFailure { throw Exception() }
        ff.value().isEmpty.shouldBeTrue()
        ff.error().orNull()!!.shouldBeInstanceOf(Exception::class.java)
    }

}