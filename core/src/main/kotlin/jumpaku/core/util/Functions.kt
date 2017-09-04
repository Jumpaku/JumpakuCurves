package jumpaku.core.util

import io.vavr.*

fun <T1, T2, R>
        function2(f: (T1, T2)->R): Function2<T1, T2, R> {
    return object : Function2<T1, T2, R> {
        override fun apply(t1: T1, t2: T2): R = f(t1, t2)
    }
}

fun <T1, T2, T3, R>
        function3(f: (T1, T2, T3)->R): Function3<T1, T2, T3, R> {
    return object : Function3<T1, T2, T3, R> {
        override fun apply(t1: T1, t2: T2, t3: T3): R = f(t1, t2, t3)
    }
}

fun <T1, T2, T3, T4, R>
        function4(f: (T1, T2, T3, T4)->R): Function4<T1, T2, T3, T4, R> {
    return object : Function4<T1, T2, T3, T4, R> {
        override fun apply(t1: T1, t2: T2, t3: T3, t4: T4): R = f(t1, t2, t3, t4)
    }
}

fun <T1, T2, T3, T4, T5, R>
        function5(f: (T1, T2, T3, T4, T5)->R): Function5<T1, T2, T3, T4, T5, R> {
    return object : Function5<T1, T2, T3, T4, T5, R> {
        override fun apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5): R = f(t1, t2, t3, t4, t5)
    }
}

fun <T1, T2, T3, T4, T5, T6, R>
        function6(f: (T1, T2, T3, T4, T5, T6)->R): Function6<T1, T2, T3, T4, T5, T6, R> {
    return object : Function6<T1, T2, T3, T4, T5, T6, R> {
        override fun apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6): R = f(t1, t2, t3, t4, t5, t6)
    }
}

fun <T1, T2, T3, T4, T5, T6, T7, R>
        function7(f: (T1, T2, T3, T4, T5, T6, T7)->R): Function7<T1, T2, T3, T4, T5, T6, T7, R> {
    return object : Function7<T1, T2, T3, T4, T5, T6, T7, R> {
        override fun apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7): R = f(t1, t2, t3, t4, t5, t6, t7)
    }
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, R>
        function8(f: (T1, T2, T3, T4, T5, T6, T7, T8)->R): Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> {
    return object : Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> {
        override fun apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7, t8: T8): R = f(t1, t2, t3, t4, t5, t6, t7, t8)
    }
}