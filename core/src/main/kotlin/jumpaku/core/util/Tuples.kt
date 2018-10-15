package jumpaku.core.util

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


operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any> Tuple5<T1, T2, T3, T4, T5>.component1(): T1 = _1()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any> Tuple5<T1, T2, T3, T4, T5>.component2(): T2 = _2()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any> Tuple5<T1, T2, T3, T4, T5>.component3(): T3 = _3()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any> Tuple5<T1, T2, T3, T4, T5>.component4(): T4 = _4()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any> Tuple5<T1, T2, T3, T4, T5>.component5(): T5 = _5()


operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any> Tuple6<T1, T2, T3, T4, T5, T6>.component1(): T1 = _1()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any> Tuple6<T1, T2, T3, T4, T5, T6>.component2(): T2 = _2()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any> Tuple6<T1, T2, T3, T4, T5, T6>.component3(): T3 = _3()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any> Tuple6<T1, T2, T3, T4, T5, T6>.component4(): T4 = _4()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any> Tuple6<T1, T2, T3, T4, T5, T6>.component5(): T5 = _5()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any> Tuple6<T1, T2, T3, T4, T5, T6>.component6(): T6 = _6()

operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component1(): T1 = _1()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component2(): T2 = _2()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component3(): T3 = _3()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component4(): T4 = _4()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component5(): T5 = _5()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component6(): T6 = _6()

operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component1(): T1 = _1()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component2(): T2 = _2()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component3(): T3 = _3()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component4(): T4 = _4()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component5(): T5 = _5()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component6(): T6 = _6()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component7(): T7 = _7()
operator fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, T6: Any, T7: Any, T8: Any> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component8(): T8 = _8()
