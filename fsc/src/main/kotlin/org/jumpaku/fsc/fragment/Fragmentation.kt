package org.jumpaku.fsc.fragment

import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.fuzzy.Grade


class Fragmentation(val t: Double = 0.025, val k: Int = 4) {
    
    fun fragment(fsc: BSpline): Array<BSpline> {
        val sample = fsc.evaluateAll(t).zip(fsc.domain.sample(t))
        val chunks = Stream.range(0, sample.size() - k)
                .map { sample.slice(it, it + k) }
                .toArray()
        val label = chunks.map {
            val last = it.last()
            val tvs = it.dropRight(1).map { includeIn(last._1, it._1) }
            val nec = tvs.map { it._1 }.min().getOrElse(Grade.TRUE)
            val pos = tvs.map { it._2 }.min().getOrElse(Grade.TRUE)
            when {
                (nec.value < 0.4 && pos.value < 0.6) -> State.MOVE
                (0.4 < nec.value && 0.6 < pos.value) -> State.STAY
                else -> State.UNKNOWN
            }
        }
        var nowState = State.STAY
        val state = label.map {
            nowState = nowState.transit(it)
            nowState
        }
        var fragments = Array<BSpline>()
        var chunkHead = chunks.head()
        var prevState = state.head()
        for (i in 1..state.size() - 1) {
            if (prevState != state[i]) {
                fragments = fragments.append(fsc.restrict(chunkHead.head()._2, chunks[i - 1].last()._2))
                chunkHead = chunks[i]
                prevState = state[i]
            }
        }
        fragments = fragments.append(fsc.restrict(chunkHead.head()._2, chunks.last().last()._2))
        return fragments
    }

    private fun includeIn(p1: Point, p2: Point): Tuple2<Grade, Grade> {
        p1.isPossible(p2)
        p1.isNecessary(p2)
        val d = p1.toCrisp().dist(p2.toCrisp())
        val f = p1.r + p2.r
        if (f.isInfinite()) return Tuple2(Grade.FALSE, Grade.TRUE)
        val nec = maxOf((p2.r - d) / f, 0.0)
        val pos = maxOf((f - d) / f, 0.0)
        return when {
            nec.isNaN() && pos.isNaN() -> Tuple2(Grade(0.5), Grade.TRUE)
            else -> Tuple2(Grade(nec), Grade(pos))
        }
    }

    enum class State {
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