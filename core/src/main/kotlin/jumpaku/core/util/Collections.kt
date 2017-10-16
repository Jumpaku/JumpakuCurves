package jumpaku.core.util

import io.vavr.collection.Seq
import io.vavr.collection.Traversable

inline val <T> Traversable<T>.lastIndex: Int get() = size() - 1

fun <T> Seq<T>.indices(): Seq<Int> = zipWithIndex { _, i -> i }

operator fun <T> Seq<T>.component1(): T = this[0]
operator fun <T> Seq<T>.component2(): T = this[1]
operator fun <T> Seq<T>.component3(): T = this[2]
operator fun <T> Seq<T>.component4(): T = this[3]
operator fun <T> Seq<T>.component5(): T = this[4]
operator fun <T> Seq<T>.component6(): T = this[5]
operator fun <T> Seq<T>.component7(): T = this[6]
operator fun <T> Seq<T>.component8(): T = this[7]
