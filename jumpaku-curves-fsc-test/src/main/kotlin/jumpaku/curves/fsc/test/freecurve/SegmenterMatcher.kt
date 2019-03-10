package jumpaku.curves.fsc.test.freecurve

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.test.identify.primitive.equals
import org.hamcrest.TypeSafeMatcher


fun equals(actual: Segmenter, expected: Segmenter): Boolean = equals(actual.identifier, expected.identifier)

fun equalTo(expected: Segmenter): TypeSafeMatcher<Segmenter> =
        matcher("equal to <$expected>") { actual -> equals(actual, expected) }
