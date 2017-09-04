package org.jumpaku.fsc.fragment

import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.fuzzy.Grade
import org.jumpaku.core.fuzzy.TruthValue


class Fragmentation(
        private val threshold: TruthValue = TruthValue(0.4, 0.6),
        private val t: Double = 0.025,
        private val k: Int = 4
) {
    
    fun fragment(fsc: BSpline): Array<BSpline> {
        val sample = fsc.evaluateAll(t).zip(fsc.domain.sample(t))
        val chunks = Stream.range(0, sample.size() - k)
                .map { sample.slice(it, it + k) }
                .toArray()
        val labels = chunks.map {
            val last = it.last()
            val tvs = it.dropRight(1).map {
                TruthValue(last._1.isNecessary(it._1), last._1.isPossible(it._1))
            }
            val nec = tvs.map { it.necessity }.min().getOrElse(Grade.TRUE)
            val pos = tvs.map { it.possibility }.min().getOrElse(Grade.TRUE)
            when {
                (nec < threshold.necessity && pos < threshold.possibility) -> State.MOVE
                (threshold.necessity < nec && threshold.possibility < pos) -> State.STAY
                else -> State.UNKNOWN
            }
        }
        val states = stateTransition(labels)
        var fragments = Array<BSpline>()
        var chunkHead = chunks.head()
        var prevState = states.head()
        for (i in 1 until states.size()) {
            if (prevState != states[i]) {
                fragments = fragments.append(fsc.restrict(chunkHead.head()._2, chunks[i - 1].last()._2))
                chunkHead = chunks[i]
                prevState = states[i]
            }
        }
        fragments = fragments.append(fsc.restrict(chunkHead.head()._2, chunks.last().last()._2))
        return fragments
    }

    private tailrec fun stateTransition(
            labels: Array<State>,
            prev: State = State.STAY,
            states: Array<State> = Array()
    ): Array<State> {
        if (labels.size() == 0) return states
        val nowState = prev.transit(labels.head())
        return stateTransition(labels.drop(1), nowState, states.append(nowState))
    }

    private enum class State {
        STAY {
            override fun transit(next: State): State {
                return when (next) {
                    STAY -> STAY
                    MOVE -> MOVE
                    UNKNOWN -> STAY
                }
            }
        },
        MOVE {
            override fun transit(next: State): State {
                return when (next) {
                    STAY -> STAY
                    MOVE -> MOVE
                    UNKNOWN -> MOVE
                }
            }
        },
        UNKNOWN {
            override fun transit(next: State): State {
                return STAY
            }
        };
        abstract fun transit(next: State): State
    }
}