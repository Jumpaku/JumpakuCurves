package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple2
import io.vavr.collection.Stream
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.fuzzy.Grade

class CandidateSelector {
    fun select(candidates: Stream<SnapCandidate>, evaluator: (SnapCandidate)->Grade): Tuple2<SnapCandidate, Grade> {
        return candidates.map { Tuple2(it, evaluator(it)) }
                .maxBy { (_, value) -> value }.get()
    }
}