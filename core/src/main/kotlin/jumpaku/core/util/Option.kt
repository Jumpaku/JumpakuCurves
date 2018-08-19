package jumpaku.core.util

sealed class Option<out T>: Iterable<T> {

    val isEmpty: Boolean get() = this === None

    val isDefined: Boolean get() = !isEmpty

    fun orNull(): T? = (this as? Some)?.value

    fun orThrow(except: ()->Exception = { NoSuchElementException("None.orThrow()") }): T =
            orNull() ?: throw except()

    fun <R> map(transform: (T) -> R): Option<R> = flatMap { Some(transform(it)) }

    fun <R> flatMap(transform: (T) -> Option<R>): Option<R> = (this as? Some)?.let { transform(value) } ?: None

    fun filter(test: (T)->Boolean): Option<T> = if (this is Some && test(value)) this else None
}

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

        override fun next(): T =
                if (hasNext()) { hasNext = false; value }
                else throw NoSuchElementException("next() of empty iterator")
    }

    override fun toString(): String = "Some($value)"
}

fun <T> Option<T>.orDefault(default: () -> T): T = orNull() ?: default()
fun <T> Option<T>.orDefault(default: T): T = orNull() ?: default

fun <T> Option<T>.toResult(): Result<T> = resultOf { orThrow() }

fun <T> none(): Option<T> = None

fun <T> some(value: T): Option<T> = Some(value)

fun <T> optionOf(nullable: T?): Option<T> = nullable?.let(::some) ?: none()

fun <T> T?.option(): Option<T> = optionOf(this)

fun <T> optionWhen(condition: Boolean, supply: () -> T): Option<T> = if (condition) some(supply()) else none()
