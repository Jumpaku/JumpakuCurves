package jumpaku.curves.fsc.test.experimental.edit;

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.experimental.edit.FscGraph
import jumpaku.curves.fsc.experimental.edit.FscPath
import jumpaku.curves.fsc.experimental.edit.Id
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: FscGraph, expected: FscGraph, error: Double = 1.0e-9): Boolean {
    val mapping: Map<Id, Id> = mutableMapOf<Id, Id>().also {
        for (vA in actual.vertices)
            for (vE in expected.vertices)
                if (isCloseTo(actual[vA]!!, expected[vE]!!, error) && vE !in it.values) {
                    it[vA] = vE
                }
    }
    return actual.vertices == mapping.keys &&
            expected.vertices == mapping.values.toSet() &&
            actual.vertices.map { mapping[it]!! }.toSet() == expected.vertices &&
            actual.edges.map { FscGraph.Edge(mapping[it.source]!!, mapping[it.destination]!!) }.toSet() == expected.edges
}


fun closeTo(expected: FscGraph, precision: Double = 1.0e-9): TypeSafeMatcher<FscGraph> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
