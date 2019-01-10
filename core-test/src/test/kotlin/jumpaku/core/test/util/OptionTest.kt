package jumpaku.core.test.util

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.toJson
import jumpaku.core.util.*
import org.amshove.kluent.*
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {

    val some = some(4)
    val none = none<Int>()

    @Test
    fun testOption() {
        println("Option")
        option(5 as Int?).isDefined.shouldBeTrue()
        option { 5 as Int? }.isDefined.shouldBeTrue()
        option(null as Int?).isEmpty.shouldBeTrue()
        option { null as Int? }.isEmpty.shouldBeTrue()
    }

    @Test
    fun testToOption() {
        println("ToOption")
        val s: Int? = 5
        val n: Int? = null
        s.toOption().isDefined.shouldBeTrue()
        n.toOption().isEmpty.shouldBeTrue()
    }

    @Test
    fun testOptionWhen() {
        println("OptionWhen")
        optionWhen(true) { 4 }.isDefined.shouldBeTrue()
        optionWhen(false) { 4 }.isEmpty.shouldBeTrue()
    }

    @Test
    fun testIsEmpty() {
        println("IsEmpty")
        some.isEmpty.shouldBeFalse()
        none.isEmpty.shouldBeTrue()
    }

    @Test
    fun testIsDefined() {
        println("IsDefined")
        some.isDefined.shouldBeTrue()
        none.isDefined.shouldBeFalse()
    }

    @Test
    fun testOrNull() {
        println("OrNull")
        some.orNull()!!.shouldEqualTo(4)
        none.orNull().shouldBeNull()
    }

    @Test
    fun testOrThrow() {
        println("OrThrow")
        some.orThrow { IllegalStateException("NG") }.shouldEqualTo(4)
        assertThrows<IllegalStateException> {
            none.orThrow { IllegalStateException("NG") }
        }
    }

    @Test
    fun testOrDefault() {
        println("OrDefault")
        some.orDefault(5).shouldEqualTo(4)
        some.orDefault { 5 }.shouldEqualTo(4)
        none.orDefault(5).shouldEqualTo(5)
        none.orDefault { 5 }.shouldEqualTo(5)
    }

    @Test
    fun testMap() {
        println("Map")
        some.map { it.toString() }.orNull()!!.shouldBeEqualTo("4")
        none.map { it.toString() }.orNull().shouldBeNull()
    }

    @Test
    fun testFlatMap() {
        println("FlatMap")
        some.flatMap { option(it.toString()) }.orNull()!!.shouldBeEqualTo("4")
        none.flatMap { option(it.toString()) }.orNull().shouldBeNull()
    }

    @Test
    fun testFilter() {
        println("Filter")
        some.filter { it.isEven() }.orNull()!!.shouldEqualTo(4)
        some.filter { it.isOdd() }.orNull().shouldBeNull()
        none.filter { it.isOdd() }.orNull().shouldBeNull()
    }

    @Test
    fun testForEach() {
        println("ForEach")
        val x0 = arrayOf(1, 2)
        some.forEach({ x0[0] = 3 }, {x0[1] = -1})
        x0[0].shouldEqualTo(3)
        x0[1].shouldEqualTo(2)

        val x1 = arrayOf(1, 2)
        none.forEach({ x1[0] = 3 }, {x1[1] = -1})
        x1[0].shouldEqualTo(1)
        x1[1].shouldEqualTo(-1)
    }
    @Test
    fun testIterator() {
        println("Iterator")

        val ls = some.toList()
        ls.size.shouldEqual(1)
        ls[0].shouldEqualTo(4)

        val ln = none.toList()
        ln.isEmpty().shouldBeTrue()
    }

    @Test
    fun testToJson() {
        println("ToJson")
        Option.fromJson(some.map { it.toJson() }.toJson()).map { it.int }.orNull()!!.shouldEqualTo(4)
        Option.fromJson(none.map { it.toJson() }.toJson()).map { it.int }.orNull().shouldBeNull()
    }

    @Test
    fun testToResult() {
        println("ToResult")
        some.toResult().value().orNull()!!.shouldEqualTo(4)
        some.toResult().error().isEmpty.shouldBeTrue()
        none.toResult().value().isEmpty.shouldBeTrue()
        none.toResult().error().orNull()!!.shouldBeInstanceOf(NoSuchElementException::class.java)
    }
}