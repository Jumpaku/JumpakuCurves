package jumpaku.curves.fsc.test.experimental.edit

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.experimental.edit.Editor
import jumpaku.curves.fsc.test.fragment.isCloseTo
import jumpaku.curves.fsc.test.merge.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Editor, expected: Editor, error: Double = 1.0e-9): Boolean =
        actual.nConnectorSamples == expected.nConnectorSamples &&
                isCloseTo(actual.connectionThreshold.value, expected.connectionThreshold.value, error) &&
                isCloseTo(actual.merger, expected.merger, error) &&
                isCloseTo(actual.fragmenter, expected.fragmenter, error)

fun closeTo(expected: Editor, precision: Double = 1.0e-9): TypeSafeMatcher<Editor> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
