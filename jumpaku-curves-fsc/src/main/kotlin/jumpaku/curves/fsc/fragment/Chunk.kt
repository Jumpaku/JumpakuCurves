package jumpaku.curves.fsc.fragment

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.fuzzy.Grade

class Chunk(samples: List<ParamPoint>) {

    class Threshold(val necessity: Grade, val possibility: Grade) {

        constructor(necessity: Double, possibility: Double) : this(Grade(necessity), Grade(possibility))
    }

    enum class Label {
        STAY, MOVE, UNKNOWN
    }

    val samples: List<ParamPoint> = samples.sortedBy { it.param }

    val beginParam: Double = this.samples.first().param

    val endParam: Double = this.samples.last().param

    val interval: Interval = Interval(beginParam, endParam)

    val necessity: Grade

    val possibility: Grade

    init {
        val last = this.samples.last().point
        val init = this.samples.dropLast(1).map { it.point }
        necessity = init.map(last::isNecessary).reduce(Grade::and)
        possibility = init.map(last::isPossible).reduce(Grade::and)
    }

    fun label(threshold: Threshold): Label = when {
        (necessity < threshold.necessity && possibility < threshold.possibility) -> Label.MOVE
        (threshold.necessity < necessity && threshold.possibility < possibility) -> Label.STAY
        else -> Label.UNKNOWN
    }
}
