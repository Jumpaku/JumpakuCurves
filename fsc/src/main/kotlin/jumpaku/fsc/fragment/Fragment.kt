package jumpaku.fsc.fragment

import jumpaku.core.curve.Interval
import jumpaku.core.curve.IntervalJson
import jumpaku.core.json.prettyGson

data class Fragment(val interval: Interval, val type: Type) {

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): FragmentJson = FragmentJson(this)

    enum class Type {
        IDENTIFICATION,
        PARTITION
    }
}

data class FragmentJson(val interval: IntervalJson, val type: Fragment.Type) {

    constructor(fragment: Fragment) : this(fragment.interval.json(), fragment.type)

    fun fragment(): Fragment = Fragment(interval.interval(), type)
}
