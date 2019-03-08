package jumpaku.curves.core.test.util

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.toJson
import jumpaku.curves.core.util.*
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {

    val some = some(4)
    val none = none<Int>()

    @Test
    fun testOption() {
        println("Option")
        assertThat(option(5 as Int?).isDefined, `is`(true))
        assertThat(option { 5 as Int? }.isDefined, `is`(true))
        assertThat(option(null as Int?).isEmpty, `is`(true))
        assertThat(option { null as Int? }.isEmpty, `is`(true))
    }

    @Test
    fun testToOption() {
        println("ToOption")
        val s: Int? = 5
        val n: Int? = null
        assertThat(s.toOption().isDefined, `is`(true))
        assertThat(n.toOption().isEmpty, `is`(true))
    }

    @Test
    fun testOptionWhen() {
        println("OptionWhen")
        assertThat(optionWhen(true) { 4 }.isDefined, `is`(true))
        assertThat(optionWhen(false) { 4 }.isEmpty, `is`(true))
    }

    @Test
    fun testIsEmpty() {
        println("IsEmpty")
        assertThat(some.isEmpty, `is`(false))
        assertThat(none.isEmpty, `is`(true))
    }

    @Test
    fun testIsDefined() {
        println("IsDefined")
        assertThat(some.isDefined, `is`(true))
        assertThat(none.isDefined, `is`(false))
    }

    @Test
    fun testOrNull() {
        println("OrNull")
        assertThat(some.orNull()!!, `is`(4))
        assertThat(none.orNull(), `is`(nullValue()))
    }

    @Test
    fun testOrThrow() {
        println("OrThrow")
        assertThat(some.orThrow { IllegalStateException("NG") }, `is`(4))
        assertThrows<IllegalStateException> { none.orThrow { IllegalStateException("NG") } }
    }

    @Test
    fun testOrDefault() {
        println("OrDefault")
        assertThat(some.orDefault(5), `is`(4))
        assertThat(some.orDefault { 5 }, `is`(4))
        assertThat(none.orDefault(5), `is`(5))
        assertThat(none.orDefault { 5 }, `is`(5))
    }

    @Test
    fun testMap() {
        println("Map")
        assertThat(some.map { it.toString() }.orNull()!!, `is`("4"))
        assertThat(none.map { it.toString() }.orNull(), `is`(nullValue()))
    }

    @Test
    fun testFlatMap() {
        println("FlatMap")
        assertThat(some.flatMap { option(it.toString()) }.orNull()!!, `is`("4"))
        assertThat(none.flatMap { option(it.toString()) }.orNull(), `is`(nullValue()))
    }

    @Test
    fun testFilter() {
        println("Filter")
        assertThat(some.filter { it.isEven() }.orNull()!!, `is`(4))
        assertThat(some.filter { it.isOdd() }.orNull(), `is`(nullValue()))
        assertThat(none.filter { it.isOdd() }.orNull(), `is`(nullValue()))
    }

    @Test
    fun testIfPresent() {
        println("IfPresent")
        val x0 = arrayOf(1, 2)
        some.ifPresent { x0[0] = 3 }
        assertThat(x0[0], `is`(3))
        assertThat(x0[1], `is`(2))

        val x1 = arrayOf(1, 2)
        none.ifPresent { x1[0] = 3 }
        assertThat(x1[0], `is`(1))
        assertThat(x1[1], `is`(2))
    }

    @Test
    fun testIfAbsent() {
        println("IfAbsent")
        val x0 = arrayOf(1, 2)
        some.ifAbsent { x0[0] = 3 }
        assertThat(x0[0], `is`(1))
        assertThat(x0[1], `is`(2))

        val x1 = arrayOf(1, 2)
        none.ifAbsent { x1[0] = 3 }
        assertThat(x1[0], `is`(3))
        assertThat(x1[1], `is`(2))
    }

    @Test
    fun testIterator() {
        println("Iterator")

        val ls = some.toList()
        assertThat(ls.size, `is`(1))
        assertThat(ls[0], `is`(4))

        val ln = none.toList()
        assertThat(ln.isEmpty(), `is`(true))
    }

    @Test
    fun testToJson() {
        println("ToJson")
        assertThat(Option.fromJson(some.map { it.toJson() }.toJson()).map { it.int }.orNull()!!, `is`(4))
        assertThat(Option.fromJson(none.map { it.toJson() }.toJson()).map { it.int }.orNull(), `is`(nullValue()))
    }

    @Test
    fun testToResult() {
        println("ToResult")
        assertThat(some.toResult().value().orNull()!!, `is`(4))
        assertThat(some.toResult().error().isEmpty, `is`(true))
        assertThat(none.toResult().value().isEmpty, `is`(true))
        assertThat(none.toResult().error().orNull()!!, `is`(instanceOf(NoSuchElementException::class.java)))
    }
}