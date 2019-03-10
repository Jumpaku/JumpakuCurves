package jumpaku.curves.core.util

import io.vavr.Tuple1
import io.vavr.Tuple2
import io.vavr.Tuple3

operator fun <T1 : Any> Tuple1<T1>.component1(): T1 = _1()

operator fun <T1 : Any, T2 : Any> Tuple2<T1, T2>.component1(): T1 = _1()
operator fun <T1 : Any, T2 : Any> Tuple2<T1, T2>.component2(): T2 = _2()

operator fun <T1 : Any, T2 : Any, T3 : Any> Tuple3<T1, T2, T3>.component1(): T1 = _1()
operator fun <T1 : Any, T2 : Any, T3 : Any> Tuple3<T1, T2, T3>.component2(): T2 = _2()
operator fun <T1 : Any, T2 : Any, T3 : Any> Tuple3<T1, T2, T3>.component3(): T3 = _3()
