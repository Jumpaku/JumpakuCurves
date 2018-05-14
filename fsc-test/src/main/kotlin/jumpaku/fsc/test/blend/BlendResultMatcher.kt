package jumpaku.fsc.test.blend

import io.vavr.API
import jumpaku.core.test.curve.bspline.isCloseTo
import jumpaku.core.test.isCloseTo
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.blend.BlendResult
import jumpaku.fsc.blend.OverlappingMatrix
import org.amshove.kluent.should

fun isCloseTo(actual: OverlappingMatrix, expected: OverlappingMatrix, error: Double = 1.0e-9): Boolean {
    val aArr = actual.matrix.flatMap { it }
    val eArr = expected.matrix.flatMap { it }
    return aArr.size() == eArr.size() &&
            aArr.zip(eArr).all { (a, e) -> isCloseTo(a.value, e.value, error) }
}

fun isCloseTo(actual: BlendResult, expected: BlendResult, error: Double = 1.0e-9): Boolean =
        actual.blended.isDefined == expected.blended.isDefined &&
                API.For(actual.blended, expected.blended).`yield` { a, e -> isCloseTo(a, e, error) }.all { it } &&
                actual.path.isDefined == expected.path.isDefined &&
                API.For(actual.path, expected.path).`yield` { a, e ->
                    isCloseTo(a.grade.value, e.grade.value, error) &&
                            a.path.zip(e.path).all { (a, e) -> a._1 == e._1 && a._2 == e._2 }
                }.all { it } &&
                isCloseTo(actual.osm, expected.osm, error)

fun BlendResult.shouldEqualToBlendResult(expected: BlendResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
