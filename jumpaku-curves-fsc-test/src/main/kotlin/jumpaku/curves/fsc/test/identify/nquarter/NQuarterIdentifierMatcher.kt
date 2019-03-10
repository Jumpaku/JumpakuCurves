package jumpaku.curves.fsc.test.identify.nquarter

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifier
import org.hamcrest.TypeSafeMatcher


fun equals(actual: NQuarterIdentifier, expected: NQuarterIdentifier): Boolean =
        actual.nSamples == expected.nSamples && actual.nFmps == expected.nFmps

fun equalTo(expected: NQuarterIdentifier): TypeSafeMatcher<NQuarterIdentifier> =
        matcher("equal to <$expected>") { actual -> equals(actual, expected) }
