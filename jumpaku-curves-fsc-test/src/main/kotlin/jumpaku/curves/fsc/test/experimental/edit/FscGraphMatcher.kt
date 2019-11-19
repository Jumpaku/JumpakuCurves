package jumpaku.curves.fsc.test.experimental.edit;

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.experimental.edit.FscGraph
import jumpaku.curves.fsc.experimental.edit.FscPath
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: FscGraph, expected: FscGraph, error: Double = 1.0e-9): Boolean =
        (actual is FscPath && expected is FscPath && isCloseTo(actual as FscPath, expected as FscPath, error)) ||
                (actual.keys == expected.keys &&
                        actual.keys.all { key -> isCloseTo(actual[key]!!, expected[key]!!, error) } &&
                        actual.decompose().zip(expected.decompose()).all { (aPath, ePath) ->
                                isCloseTo(aPath, ePath, error)
                        })


fun closeTo(expected: FscGraph, precision: Double = 1.0e-9): TypeSafeMatcher<FscGraph> =
        matcher("close to <$expected> with precision $precision") { actual ->
                if (actual is FscPath && expected is FscPath)
                        isCloseTo(actual as FscPath, expected as FscPath, precision)
                else isCloseTo(actual, expected, precision)
        }
