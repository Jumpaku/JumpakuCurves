package jumpaku.curves.fsc.blend

import jumpaku.curves.core.fuzzy.Grade


sealed class OverlapState {

    class Path(val grade: Grade, delegate: List<OverlapMatrix.Key>) : List<OverlapMatrix.Key> by delegate

    abstract val osm: OverlapMatrix

    class NotDetected(override val osm: OverlapMatrix) : OverlapState()

    class Detected(override val osm: OverlapMatrix, val front: Path, val middle: Path, val back: Path) :
        OverlapState()

    val isDetected: Boolean get() = this is Detected
}

