package jumpaku.curves.fsc.test.snap.conicsection

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.core.test.curve.bezier.isCloseTo
import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapResult
import jumpaku.curves.fsc.test.snap.point.isCloseTo
import org.hamcrest.TypeSafeMatcher


fun isCloseTo(actual: ConicSectionSnapResult.Candidate, expected: ConicSectionSnapResult.Candidate, error: Double): Boolean =
        actual.featurePoints.size == expected.featurePoints.size &&
                actual.featurePoints.zip(expected.featurePoints).all { (a, e) ->
                    isCloseTo(a.source, e.source, error) &&
                            if (a.target.isDefined && e.target.isDefined) isCloseTo(a.target.orThrow(), e.target.orThrow(), error)
                            else a.target.isDefined == e.target.isDefined

                }

fun isCloseTo(actual: ConicSectionSnapResult, expected: ConicSectionSnapResult, error: Double): Boolean =
        actual.snappedConicSection.isDefined == expected.snappedConicSection.isDefined &&
                actual.snappedConicSection.isEmpty ||
                (actual.snappedConicSection.isDefined &&
                        isCloseTo(actual.snappedConicSection.orThrow(), expected.snappedConicSection.orThrow(), error)) &&
                actual.candidates.size == expected.candidates.size &&
                actual.candidates.zip(expected.candidates).all { (a, e) ->
                    isCloseTo(a.grade.value, e.grade.value) &&
                            isCloseTo(a.candidate, e.candidate, error)
                }

fun closeTo(expected: ConicSectionSnapResult, precision: Double = 1.0e-9): TypeSafeMatcher<ConicSectionSnapResult> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
