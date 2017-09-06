package jumpaku.fsc.fragment

import io.vavr.collection.Array
import jumpaku.core.json.prettyGson

data class FragmentResult(val fragments: Array<Fragment>) {

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): FragmentResultJson = FragmentResultJson(this)
}

data class FragmentResultJson(val fragments: Array<FragmentJson>) {

    constructor(fragmentResult: FragmentResult) : this(fragmentResult.fragments.map { it.json() })

    fun fragmentResult(): FragmentResult = FragmentResult(fragments.map { it.fragment() })
}