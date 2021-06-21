package jumpaku.curves.fsc.test.blend

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.test.generate.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Blender, expected: Blender, error: Double = 1.0e-9): Boolean =
        actual.degree == expected.degree &&
                isCloseTo(actual.knotSpan, expected.knotSpan, error) &&
                actual.extendDegree == expected.extendDegree &&
                isCloseTo(actual.extendInnerSpan, expected.extendInnerSpan, error) &&
                isCloseTo(actual.extendOuterSpan, expected.extendOuterSpan, error) &&
                isCloseTo(actual.overlapThreshold.value, expected.overlapThreshold.value, error) &&
                isCloseTo(actual.samplingSpan, expected.samplingSpan, error) &&
                isCloseTo(actual.blendRate, expected.blendRate, error) &&
                isCloseTo(actual.bandWidth, expected.bandWidth, error) &&
                isCloseTo(actual.fuzzifier, expected.fuzzifier, error)

fun closeTo(expected: Blender, precision: Double = 1.0e-9): TypeSafeMatcher<Blender> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
