package jumpaku.curves.fsc.fragment

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade


class Fragmenter(
        val threshold: Chunk.Threshold = Chunk.Threshold(
                necessity = Grade(0.35),
                possibility = Grade(0.65)),
        val chunkSize: Int = 4,
        val minStayTimeSpan: Double = 0.04) : ToJson {

    private enum class State {

        STAY {
            override fun transit(next: Chunk.Label): Fragmenter.State = when (next) {
                Chunk.Label.STAY, Chunk.Label.UNKNOWN -> Fragmenter.State.STAY
                Chunk.Label.MOVE -> Fragmenter.State.MOVE
            }
        },
        MOVE {
            override fun transit(next: Chunk.Label): Fragmenter.State = when (next) {
                Chunk.Label.STAY -> Fragmenter.State.STAY
                Chunk.Label.MOVE, Chunk.Label.UNKNOWN -> Fragmenter.State.MOVE
            }
        };

        abstract fun transit(next: Chunk.Label): Fragmenter.State
    }

    init {
        require(chunkSize >= 2) { "k should be k >= 2" }
        require(minStayTimeSpan.isFinite()) { "minStayTimeSpan($minStayTimeSpan) must be finite" }
        require(minStayTimeSpan > 0.0) { "minStayTimeSpan($minStayTimeSpan) must be positive" }
        require(threshold.necessity <= threshold.necessity) {
            "must be necessityThreshold(${threshold.necessity}) <= possibilityThreshold(${threshold.necessity})"
        }
    }

    fun fragment(fsc: BSpline): List<Fragment> {
        val samplingSpan = minStayTimeSpan / chunkSize
        val chunks = fsc.domain.sample(samplingSpan)
                .windowed(chunkSize)
                .map { Chunk(fsc.restrict(it.first(), it.last()).sample(chunkSize)) }
        val states = chunks
                .map { it.label(threshold) }
                .fold(mutableListOf(Fragmenter.State.STAY)) { l, n -> l.apply { add(l.last().transit(n)) } }
                .drop(1)
        val initial = chunks.first().run { Triple(beginParam, endParam, states.first()) }
        return chunks.zip(states)
                .fold(mutableListOf(initial)) { prev, (nextChunk, nextState) ->
                    prev.apply {
                        val (prevBegin, _, prevState) = last()
                        if (prevState != nextState) add(Triple(nextChunk.beginParam, nextChunk.endParam, nextState))
                        else set(prev.lastIndex, Triple(prevBegin, nextChunk.endParam, prevState))
                    }
                }.map { (begin, end, state) ->
                    when (state!!) {  // 型推論がうまくいかない
                        State.MOVE -> Fragment(Interval(begin, end), Fragment.Type.Move)
                        State.STAY -> Fragment(Interval(begin, end), Fragment.Type.Stay)
                    }
                }
    }

    override fun toJson(): JsonElement = jsonObject(
            "necessityThreshold" to threshold.necessity.toJson(),
            "possibilityThreshold" to threshold.possibility.toJson(),
            "chunkSize" to chunkSize.toJson(),
            "minStayTimeSpan" to minStayTimeSpan.toJson())

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): Fragmenter = Fragmenter(
                Chunk.Threshold(
                        Grade.fromJson(json["necessityThreshold"].asJsonPrimitive),
                        Grade.fromJson(json["possibilityThreshold"].asJsonPrimitive)),
                json["chunkSize"].int,
                json["minStayTimeSpan"].double)
    }

}
