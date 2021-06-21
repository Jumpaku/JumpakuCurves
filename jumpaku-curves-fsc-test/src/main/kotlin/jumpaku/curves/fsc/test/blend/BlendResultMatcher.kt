package jumpaku.curves.fsc.test.blend

import jumpaku.commons.test.matcher
import jumpaku.curves.core.test.curve.bspline.isCloseTo
import jumpaku.curves.fsc.blend.BlendResult
import org.hamcrest.TypeSafeMatcher


fun isCloseTo(actual: BlendResult, expected: BlendResult, error: Double = 1.0e-9): Boolean =
    when (actual) {
        is BlendResult.Blended -> expected is BlendResult.Blended &&
                isCloseTo(actual.overlapState, expected.overlapState, error) &&
                isCloseTo(actual.blended, expected.blended, error)
        is BlendResult.NotBlended -> expected is BlendResult.Blended &&
                isCloseTo(actual.overlapState, expected.overlapState, error)
    }

fun closeTo(expected: BlendResult, precision: Double = 1.0e-9): TypeSafeMatcher<BlendResult> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }
