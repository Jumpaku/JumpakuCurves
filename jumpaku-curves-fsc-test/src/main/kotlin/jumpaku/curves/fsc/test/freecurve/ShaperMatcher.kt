package jumpaku.curves.fsc.test.freecurve

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.freecurve.Shaper
import jumpaku.curves.fsc.freecurve.Smoother
import jumpaku.curves.fsc.test.identify.primitive.equals
import org.hamcrest.TypeSafeMatcher


fun equals(actual: Segmenter, expected: Segmenter): Boolean = equals(actual.identifier, expected.identifier)

fun equalTo(expected: Segmenter): TypeSafeMatcher<Segmenter> =
        matcher("equal to <$expected>") { actual -> equals(actual, expected) }


fun isCloseTo(actual: Smoother, expected: Smoother, error: Double = 1.0e-9): Boolean =
        actual.samplingFactor == expected.samplingFactor &&
                isCloseTo(actual.pruningFactor, expected.pruningFactor, error)

fun closeTo(expected: Smoother, precision: Double = 1.0e-9): TypeSafeMatcher<Smoother> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }


fun isCloseTo(actual: Shaper, expected: Shaper, error: Double = 1.0e-9): Boolean =
        equals(actual.segmenter, expected.segmenter) &&
                isCloseTo(actual.smoother, expected.smoother, error) &&
                actual.sampleMethod.run {
                    val e = expected.sampleMethod
                    when (this) {
                        is Shaper.SampleMethod.ByFixedNumber ->
                            e is Shaper.SampleMethod.ByFixedNumber && nSamples == e.nSamples
                        is Shaper.SampleMethod.ByEqualInterval ->
                            e is Shaper.SampleMethod.ByEqualInterval && isCloseTo(samplingSpan, e.samplingSpan, error)
                        else -> false
                    }
                }

fun closeTo(expected: Shaper, precision: Double = 1.0e-9): TypeSafeMatcher<Shaper> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
