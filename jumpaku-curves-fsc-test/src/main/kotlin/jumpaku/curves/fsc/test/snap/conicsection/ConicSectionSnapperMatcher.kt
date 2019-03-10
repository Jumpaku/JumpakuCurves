package jumpaku.curves.fsc.test.snap.conicsection

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.curves.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.curves.fsc.test.snap.point.equals
import org.hamcrest.TypeSafeMatcher

fun equals(actual: ConicSectionSnapper<ConjugateCombinator>, expected: ConicSectionSnapper<ConjugateCombinator>): Boolean =
        equals(actual.pointSnapper, expected.pointSnapper)

fun equalTo(expected: ConicSectionSnapper<ConjugateCombinator>): TypeSafeMatcher<ConicSectionSnapper<ConjugateCombinator>> =
        matcher("equal to <$expected>") { actual -> equals(actual, expected) }
