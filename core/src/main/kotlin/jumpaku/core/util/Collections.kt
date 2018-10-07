package jumpaku.core.util

import io.vavr.collection.*
import io.vavr.collection.Array

inline val <T: Any> Traversable<T>.lastIndex: Int get() = size() - 1

fun <T: Any> Seq<T>.indices(): Seq<Int> = zipWithIndex { _, i -> i }

operator fun <T: Any> Seq<T>.component1(): T = this[0]
operator fun <T: Any> Seq<T>.component2(): T = this[1]
operator fun <T: Any> Seq<T>.component3(): T = this[2]
operator fun <T: Any> Seq<T>.component4(): T = this[3]
operator fun <T: Any> Seq<T>.component5(): T = this[4]
operator fun <T: Any> Seq<T>.component6(): T = this[5]
operator fun <T: Any> Seq<T>.component7(): T = this[6]
operator fun <T: Any> Seq<T>.component8(): T = this[7]

fun <E: Any> List<E>.asVavr(): Array<E> = Array.ofAll(this)
fun <E: Any> Array<E>.asKt(): List<E> = asJava()

fun <K: Any, V: Any> Map<K, V>.asVavr(): io.vavr.collection.Map<K, V> = io.vavr.collection.HashMap.ofAll(this)
fun <K: Any, V: Any> io.vavr.collection.Map<K, V>.asKt(): Map<K, V> = toJavaMap()
