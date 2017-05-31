package org.jumpaku.core.util

import io.vavr.*

operator fun <T1> Tuple1<T1>.component1(): T1 = this._1()

operator fun <T1, T2> Tuple2<T1, T2>.component1(): T1 = this._1()
operator fun <T1, T2> Tuple2<T1, T2>.component2(): T2 = this._2()

operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component1(): T1 = this._1()
operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component2(): T2 = this._2()
operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component3(): T3 = this._3()

operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component1(): T1 = this._1()
operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component2(): T2 = this._2()
operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component3(): T3 = this._3()
operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component4(): T4 = this._4()


operator fun <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5>.component1(): T1 = this._1()
operator fun <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5>.component2(): T2 = this._2()
operator fun <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5>.component3(): T3 = this._3()
operator fun <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5>.component4(): T4 = this._4()
operator fun <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5>.component5(): T5 = this._5()


operator fun <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6>.component1(): T1 = this._1()
operator fun <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6>.component2(): T2 = this._2()
operator fun <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6>.component3(): T3 = this._3()
operator fun <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6>.component4(): T4 = this._4()
operator fun <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6>.component5(): T5 = this._5()
operator fun <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6>.component6(): T6 = this._6()

operator fun <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component1(): T1 = this._1()
operator fun <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component2(): T2 = this._2()
operator fun <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component3(): T3 = this._3()
operator fun <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component4(): T4 = this._4()
operator fun <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component5(): T5 = this._5()
operator fun <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7>.component6(): T6 = this._6()

operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component1(): T1 = this._1()
operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component2(): T2 = this._2()
operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component3(): T3 = this._3()
operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component4(): T4 = this._4()
operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component5(): T5 = this._5()
operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component6(): T6 = this._6()
operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component7(): T7 = this._7()
operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>.component8(): T8 = this._8()
