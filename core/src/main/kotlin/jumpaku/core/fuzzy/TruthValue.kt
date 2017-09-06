package jumpaku.core.fuzzy


data class TruthValue(val necessity: Grade, val possibility: Grade) {

    constructor(necessity: Double, possibility: Double) : this(Grade(necessity), Grade(possibility))
}