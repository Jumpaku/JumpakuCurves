package jumpaku.curves.fsc.fragment

import jumpaku.curves.core.curve.Interval

data class Fragment(val interval: Interval, val type: Type) {

    override fun toString(): String = "Fragment(interval=$interval,type=$type)"

    enum class Type {
        Move, Stay
    }
}

