package jumpaku.curves.fsc.test.blend

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.blend.OverlapState
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: OverlapState.Path, expected: OverlapState.Path, error: Double = 1.0e-9): Boolean =
    actual.size == expected.size && actual.zip(expected).all { (a, e) -> a == e } &&
            jumpaku.commons.math.test.isCloseTo(actual.grade.value, expected.grade.value, error)

fun closeTo(expected: OverlapState.Path, precision: Double = 1.0e-9): TypeSafeMatcher<OverlapState.Path> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }

fun isCloseTo(actual: OverlapState, expected: OverlapState, error: Double = 1.0e-9): Boolean =
    isCloseTo(actual.osm, expected.osm, error) &&
            when (actual) {
                is OverlapState.NotDetected -> expected is OverlapState.Detected
                is OverlapState.Detected -> expected is OverlapState.Detected &&
                        isCloseTo(actual.front, expected.front, error) &&
                        isCloseTo(actual.middle, expected.middle, error) &&
                        isCloseTo(actual.back, expected.back, error)
            }

fun closeTo(expected: OverlapState, precision: Double = 1.0e-9): TypeSafeMatcher<OverlapState> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }