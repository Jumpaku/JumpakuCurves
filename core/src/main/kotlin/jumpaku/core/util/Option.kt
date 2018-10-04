package jumpaku.core.util

import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.jsonNull
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonObject


sealed class Option<out T>: Iterable<T> {

    val isEmpty: Boolean get() = this === None

    val isDefined: Boolean get() = !isEmpty

    fun orNull(): T? = (this as? Some)?.value

    fun orThrow(except: ()->Exception = { NoSuchElementException("None.orThrow()") }): T = orNull() ?: throw except()

    fun <U> map(transform: (T) -> U): Option<U> = flatMap { Some(transform(it)) }

    fun <U> flatMap(transform: (T) -> Option<U>): Option<U> = (this as? Some)?.let { transform(value) } ?: None

    fun filter(test: (T)->Boolean): Option<T> = if (this is Some && test(value)) this else None

    companion object {

        fun fromJson(json: JsonElement): Result<Option<JsonElement>> = result {
            optionWhen("value" in (json as JsonObject)) { json["value"] }
        }

        fun <T> fromJson(json: JsonElement, transform: (JsonElement) -> T): Result<Option<T>> =
                fromJson(json).tryMap { it.map(transform) }
    }
}

fun Option<JsonElement>.toJson(): JsonElement = map {
    jsonObject("value" to it)
}.orDefault(jsonNull)

object None : Option<Nothing>() {

    object NoneIterator : Iterator<Nothing> {

        override fun hasNext(): Boolean = false

        override fun next(): Nothing = throw NoSuchElementException("next() of empty iterator")
    }

    override fun iterator(): Iterator<Nothing> = NoneIterator

    override fun toString(): String = "None"
}

class Some<out T>(val value: T) : Option<T>() {

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

fun <T> Option<Option<T>>.flatten(): Option<T> = flatMap { it }

fun <T> Option<T>.orDefault(default: () -> T): T = orNull() ?: default()
fun <T> Option<T>.orDefault(default: T): T = orNull() ?: default

fun <T> Option<T>.toResult(): Result<T> = result { orThrow() }

fun <T> none(): Option<T> = None

fun <T> some(value: T): Option<T> = Some(value)

fun <T> option(nullable: T?): Option<T> = option { nullable }
fun <T> option(nullable: ()->T?): Option<T> = nullable()?.let(::some) ?: none()

fun <T> T?.toOption(): Option<T> = option { this }

fun <T> optionWhen(condition: Boolean, supply: () -> T): Option<T> = if (condition) some(supply()) else none()
