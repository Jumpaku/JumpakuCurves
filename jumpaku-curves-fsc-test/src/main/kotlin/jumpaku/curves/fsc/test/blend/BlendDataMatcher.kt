package jumpaku.curves.fsc.test.blend

import jumpaku.commons.test.matcher
import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.curves.fsc.blend.BlendData
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: BlendData, expected: BlendData, error: Double = 1.0e-9): Boolean =
        actual.front.size == expected.front.size &&
                actual.front.zip(expected.front).all { (a, e) -> isCloseTo(a, e, error) } &&
                actual.back.size == expected.back.size &&
                actual.back.zip(expected.back).all { (a, e) -> isCloseTo(a, e, error) } &&
                actual.blended.size == expected.blended.size &&
                actual.blended.zip(expected.blended).all { (a, e) -> isCloseTo(a, e, error) }

fun closeTo(expected: BlendData, precision: Double = 1.0e-9): TypeSafeMatcher<BlendData> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
