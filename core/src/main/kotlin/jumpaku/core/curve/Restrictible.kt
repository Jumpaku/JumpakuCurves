package jumpaku.core.curve



interface Restrictible<out C> {

    fun restrict(i: Interval): C = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): C
}