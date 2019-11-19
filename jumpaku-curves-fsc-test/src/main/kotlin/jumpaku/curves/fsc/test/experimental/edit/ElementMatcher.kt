package jumpaku.curves.fsc.test.experimental.edit

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.experimental.edit.Element
import org.hamcrest.TypeSafeMatcher


fun isCloseTo(actual: Element, expected: Element, error: Double = 1.0e-9): Boolean =
        when(actual){
            is Element.Connector -> expected is Element.Connector &&
                    jumpaku.curves.core.test.geom.isCloseTo(actual.body, expected.body, error) &&
                    actual.front.isDefined == expected.front.isDefined &&
                    (if (actual.front.isDefined) jumpaku.curves.core.test.geom.isCloseTo(actual.front.orThrow(), expected.front.orThrow(), error) else true) &&
                    actual.back.isDefined == expected.back.isDefined &&
                    (if (actual.back.isDefined) jumpaku.curves.core.test.geom.isCloseTo(actual.back.orThrow(), expected.back.orThrow(), error) else true)
            is Element.Target -> expected is Element.Target &&
                    jumpaku.curves.core.test.curve.bspline.isCloseTo(actual.fragment, expected.fragment, error)
        }

fun closeTo(expected: Element, precision: Double = 1.0e-9): TypeSafeMatcher<Element> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
