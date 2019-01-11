package jumpaku.core.util

import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.jsonNull
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonObject


sealed class Option<out T: Any>: Iterable<T> {

    val isEmpty: Boolean get() = this === None

    val isDefined: Boolean get() = !isEmpty

    fun orNull(): T? = (this as? Some)?.value

    fun orThrow(except: ()->Exception = { NoSuchElementException("None.orThrow()") }): T = orNull() ?: throw except()

    fun <U: Any> map(transform: (T) -> U): Option<U> = flatMap { Some(transform(it)) }

    fun <U: Any> flatMap(transform: (T) -> Option<U>): Option<U> = (this as? Some)?.let { transform(value) } ?: None

    fun filter(test: (T)->Boolean): Option<T> = if (this is Some && test(value)) this else None

    fun forEach(actionIfPresent: (T)->Unit, actionIfAbsent: ()->Unit): Unit = when(this) {
        is Some -> actionIfPresent(value)
        is None -> actionIfAbsent()
    }

    companion object {

        fun fromJson(json: JsonElement): Option<JsonElement> =
            if ("value" in (json as JsonObject) && !json["value"].isJsonNull) some(json["value"])
            else none()

        fun <T: Any> fromJson(json: JsonElement, transform: (JsonElement) -> T): Option<T> = fromJson(json).map(transform)
    }
}

fun <J : JsonElement> Option<J>.toJson(): JsonElement = map {
    jsonObject("value" to it)
}.orDefault(jsonObject("value" to jsonNull))

object None : Option<Nothing>() {

    object NoneIterator : Iterator<Nothing> {

        override fun hasNext(): Boolean = false

        override fun next(): Nothing = throw NoSuchElementException("next() of empty iterator")
    }

    override fun iterator(): Iterator<Nothing> = NoneIterator

    override fun toString(): String = "None"
}

class Some<out T: Any>(val value: T) : Option<T>() {

    override fun iterator(): Iterator<T> = object : Iterator<T> {

        var hasNext: Boolean = true

        override fun hasNext(): Boolean = hasNext

        override fun next(): T = when {
            hasNext() -> { hasNext = false; value }
            else -> throw NoSuchElementException("next() of empty iterator")
        }
    }

    override fun toString(): String = "Some($value)"
}

fun <T: Any> Option<Option<T>>.flatten(): Option<T> = flatMap { it }

fun <T: Any> Option<T>.orDefault(default: () -> T): T = orNull() ?: default()
fun <T: Any> Option<T>.orDefault(default: T): T = orNull() ?: default

fun <T: Any> Option<T>.toResult(except: () -> Exception = { NoSuchElementException("None.orThrow()") }): Result<T> =
        result { (this as? Some)?.value ?: throw except() }

fun <T: Any> none(): Option<T> = None

fun <T: Any> some(value: T): Option<T> = Some(value)

fun <T: Any> option(nullable: T?): Option<T> = option { nullable }
fun <T: Any> option(nullable: ()->T?): Option<T> = nullable()?.let(::some) ?: none()

fun <T: Any> T?.toOption(): Option<T> = option { this }

fun <T: Any> optionWhen(condition: Boolean, supply: () -> T): Option<T> = if (condition) some(supply()) else none()

fun <T: Any> Option<T>.asVavr(): io.vavr.control.Option<T> =
        (this as? Some)?.run { io.vavr.control.Option.some(value) } ?: io.vavr.control.Option.none()
fun <T: Any> io.vavr.control.Option<T>.toJOpt(): Option<T> = orNull.toOption()

