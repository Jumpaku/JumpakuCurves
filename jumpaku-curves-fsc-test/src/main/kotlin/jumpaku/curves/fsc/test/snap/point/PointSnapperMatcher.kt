package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.snap.point.IFGS
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.fsc.snap.point.PointSnapper
import org.hamcrest.TypeSafeMatcher

fun equals(actual: PointSnapper, expected: PointSnapper): Boolean = when (actual) {
    is MFGS -> expected is MFGS &&
            actual.minResolution == expected.minResolution &&
            actual.maxResolution == expected.maxResolution
    is IFGS -> expected is IFGS
    else -> error("Invalid Class of PointSnapper")
}

fun equalTo(expected: PointSnapper): TypeSafeMatcher<PointSnapper> =
        matcher("equal to <$expected>") { actual -> equals(actual, expected) }
