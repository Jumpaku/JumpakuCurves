package jumpaku.curves.fsc.test.blend

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.blend.Blender
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Blender, expected: Blender, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.samplingSpan, expected.samplingSpan, error) &&
                isCloseTo(actual.blendingRate, expected.blendingRate, error) &&
                isCloseTo(actual.threshold.value, expected.threshold.value, error)

fun closeTo(expected: Blender, precision: Double = 1.0e-9): TypeSafeMatcher<Blender> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
