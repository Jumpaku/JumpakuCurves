package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.Primitive7Identifier
import org.hamcrest.TypeSafeMatcher

fun equals(actual: Open4Identifier, expected: Open4Identifier): Boolean =
        actual.nSamples == expected.nSamples && actual.nFmps == expected.nFmps

fun equalTo(expected: Open4Identifier): TypeSafeMatcher<Open4Identifier> =
        matcher("equal to <$expected>") { actual -> equals(actual, expected) }


fun equals(actual: Primitive7Identifier, expected: Primitive7Identifier): Boolean =
        actual.nSamples == expected.nSamples && actual.nFmps == expected.nFmps

fun equalTo(expected: Primitive7Identifier): TypeSafeMatcher<Primitive7Identifier> =
        matcher("equal to <$expected>") { actual -> equals(actual, expected) }
