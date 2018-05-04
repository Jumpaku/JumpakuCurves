package jumpaku.fsc.test.blend

import jumpaku.core.test.curve.bspline.bSplineAssertThat
import jumpaku.fsc.blend.BlendResult
import jumpaku.fsc.blend.OverlappingMatrix
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun overlappingMatrixAssertThat(actual: OverlappingMatrix): OverlappingMatrixAssert = OverlappingMatrixAssert(actual)
class OverlappingMatrixAssert(actual: OverlappingMatrix)
    : AbstractAssert<OverlappingMatrixAssert, OverlappingMatrix>(actual, OverlappingMatrixAssert::class.java) {

    fun isEqualToOverlappingMatrix(expected: OverlappingMatrix, eps: Double = 1.0e-10): OverlappingMatrixAssert {
        isNotNull

        Assertions.assertThat(actual.rowSize).isEqualTo(expected.rowSize)
        Assertions.assertThat(actual.columnSize).isEqualTo(expected.columnSize)
        for (j in 0..actual.rowLastIndex){
            for (k in 0..actual.columnLastIndex) {
                Assertions.assertThat(actual[j, k].value).isEqualTo(expected[j, k].value, Assertions.withPrecision(eps))
            }
        }

        return this
    }
}

fun blendResultAssertThat(actual: BlendResult): BlendResultAssert = BlendResultAssert(actual)
class BlendResultAssert(actual: BlendResult) : AbstractAssert<BlendResultAssert, BlendResult>(actual, BlendResultAssert::class.java) {

    fun isEqualToBlendResult(expected: BlendResult, eps: Double = 1.0e-10): BlendResultAssert {
        isNotNull

        overlappingMatrixAssertThat(actual.osm).isEqualToOverlappingMatrix(expected.osm, eps)

        Assertions.assertThat(actual.blended.isDefined).isEqualTo(expected.blended.isDefined)
        Assertions.assertThat(actual.path.isDefined).isEqualTo(expected.path.isDefined)
        Assertions.assertThat(actual.path.isDefined == actual.blended.isDefined).isTrue()
        if(actual.path.isEmpty){
            return this
        }
        bSplineAssertThat(actual.blended.get()).isEqualToBSpline(expected.blended.get(), eps)
        Assertions.assertThat(actual.path.get().grade.value).isEqualTo(expected.path.get().grade.value, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.path.get().path.size()).isEqualTo(expected.path.get().path.size())
        for (j in 0 until actual.path.get().path.size()) {
            Assertions.assertThat(actual.path.get().path[j]._1).isEqualTo(expected.path.get().path[j]._1)
            Assertions.assertThat(actual.path.get().path[j]._2).isEqualTo(expected.path.get().path[j]._2)
        }

        return this
    }
}