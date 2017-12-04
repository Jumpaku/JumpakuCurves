package jumpaku.fsc.fragment

import jumpaku.core.fuzzy.Grade


data class TruthValueThreshold(val necessity: Grade, val possibility: Grade) {

    constructor(necessity: Double, possibility: Double) : this(Grade(necessity), Grade(possibility))
}