package jumpaku.curves.fsc.test.merge

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.curves.fsc.merge.MergeData
import jumpaku.curves.fsc.test.generate.fit.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: MergeData, expected: MergeData, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.grade.value, expected.grade.value, error) &&
        actual.front.size == expected.front.size &&
                actual.front.zip(expected.front).all { (a, e) -> isCloseTo(a, e, error) } &&
                actual.back.size == expected.back.size &&
                actual.back.zip(expected.back).all { (a, e) -> isCloseTo(a, e, error) } &&
                actual.merged.size == expected.merged.size &&
                actual.merged.zip(expected.merged).all { (a, e) -> isCloseTo(a, e, error) }

fun closeTo(expected: MergeData, precision: Double = 1.0e-9): TypeSafeMatcher<MergeData> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
