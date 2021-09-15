package jumpaku.curves.core.test.curve

import jumpaku.commons.test.matcher
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.test.geom.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(
    actual: Curve,
    expected: Curve,
    nSamples: Int = 100,
    error: Double = 1.0e-9
): Boolean {
    if (!isCloseTo(actual.domain, expected.domain, error)) return false
    val a = actual(Sampler(nSamples))
    val e = expected(Sampler(nSamples))
    return a.size == e.size && a.zip(e).all { (ai, ei) -> isCloseTo(ai, ei, error) }
}

fun closeTo(
    expected: Curve,
    nSamples: Int = 100,
    precision: Double = 1.0e-9
): TypeSafeMatcher<Curve> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, nSamples, precision)
    }