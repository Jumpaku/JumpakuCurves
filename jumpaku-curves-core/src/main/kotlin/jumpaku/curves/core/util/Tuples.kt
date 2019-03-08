package jumpaku.curves.core.util

import io.vavr.*

operator fun <T1: Any> Tuple1<T1>.component1(): T1 = _1()

operator fun <T1: Any, T2: Any> Tuple2<T1, T2>.component1(): T1 = _1()
operator fun <T1: Any, T2: Any> Tuple2<T1, T2>.component2(): T2 = _2()

operator fun <T1: Any, T2: Any, T3: Any> Tuple3<T1, T2, T3>.component1(): T1 = _1()
operator fun <T1: Any, T2: Any, T3: Any> Tuple3<T1, T2, T3>.component2(): T2 = _2()
operator fun <T1: Any, T2: Any, T3: Any> Tuple3<T1, T2, T3>.component3(): T3 = _3()

operator fun <T1: Any, T2: Any, T3: Any, T4: Any> Tuple4<T1, T2, T3, T4>.component1(): T1 = _1()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any> Tuple4<T1, T2, T3, T4>.component2(): T2 = _2()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any> Tuple4<T1, T2, T3, T4>.component3(): T3 = _3()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any> Tuple4<T1, T2, T3, T4>.component4(): T4 = _4()
