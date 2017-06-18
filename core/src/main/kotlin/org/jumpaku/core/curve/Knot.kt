package org.jumpaku.core.curve

import org.jumpaku.core.json.prettyGson


data class Knot(val value: Double, val multiplicity: Int = 1) {

    init {
        require(multiplicity > 0) { "negative multiplicity($multiplicity)" }
    }

    fun reduce(m: Int = 1): Knot = Knot(value, multiplicity - m)

    fun multiply(m: Int = 1): Knot = Knot(value, multiplicity + m)

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): KnotJson = KnotJson(this)
}

data class KnotJson(private val value: Double, private val multiplicity: Int){

    constructor(knot: Knot) : this(knot.value, knot.multiplicity)

    fun  knot(): Knot = Knot(value, multiplicity)
}