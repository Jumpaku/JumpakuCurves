package jumpaku.curves.core.util

import io.vavr.collection.Array
import io.vavr.collection.Seq
import io.vavr.collection.Traversable

inline val <T : Any> Traversable<T>.lastIndex: Int get() = size() - 1

operator fun <T : Any> Seq<T>.component1(): T = this[0]
operator fun <T : Any> Seq<T>.component2(): T = this[1]
operator fun <T : Any> Seq<T>.component3(): T = this[2]

fun <E : Any> List<E>.asVavr(): Array<E> = Array.ofAll(this)
fun <E : Any> Array<E>.asKt(): List<E> = asJava()
