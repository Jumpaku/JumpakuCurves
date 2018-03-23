package jumpaku.fsc.blend

import jumpaku.core.curve.bspline.bSplineAssertThat
import jumpaku.core.json.parseJson
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.junit.Test
import java.nio.file.Paths

fun overlappingMatrixAssertThat(actual: OverlappingMatrix): OverlappingMatrixAssert = OverlappingMatrixAssert(actual)

class OverlappingMatrixAssert(actual: OverlappingMatrix)
    : AbstractAssert<OverlappingMatrixAssert, OverlappingMatrix>(actual, OverlappingMatrixAssert::class.java) {

    fun isEqualToOverlappingMatrix(expected: OverlappingMatrix, eps: Double = 1.0e-10): OverlappingMatrixAssert {
        isNotNull

        assertThat(actual.rowSize).isEqualTo(expected.rowSize)
        assertThat(actual.columnSize).isEqualTo(expected.columnSize)
        for (j in 0..actual.rowLastIndex){
            for (k in 0..actual.columnLastIndex) {
                assertThat(actual[j, k].value).isEqualTo(expected[j, k].value, withPrecision(eps))
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

        assertThat(actual.blended.isDefined).isEqualTo(expected.blended.isDefined)
        assertThat(actual.path.isDefined).isEqualTo(expected.path.isDefined)
        assertThat(actual.path.isDefined == actual.blended.isDefined).isTrue()
        if(actual.path.isEmpty){
            return this
        }
        bSplineAssertThat(actual.blended.get()).isEqualToBSpline(expected.blended.get(), eps)
        assertThat(actual.path.get().grade.value).isEqualTo(expected.path.get().grade.value, withPrecision(eps))
        assertThat(actual.path.get().path.size()).isEqualTo(expected.path.get().path.size())
        for (j in 0 until actual.path.get().path.size()) {
            assertThat(actual.path.get().path[j]._1).isEqualTo(expected.path.get().path[j]._1)
            assertThat(actual.path.get().path[j]._2).isEqualTo(expected.path.get().path[j]._2)
        }
        overlappingMatrixAssertThat(actual.path.get().osm).isEqualToOverlappingMatrix(expected.path.get().osm, eps)

        return this
    }
}

class BlendResultTest {

    val path = Paths.get("./src/test/resources/jumpaku/fsc/blend/")

    @Test
    fun testToString() {
        println("ToString")
        for (i in 0..4) {
            val e = path.resolve("BlendResult$i.json").parseJson().flatMap { BlendResult.fromJson(it) }.get()
            val a = e.toJson().let { BlendResult.fromJson(it) }.get()
            blendResultAssertThat(a).isEqualToBlendResult(e)
        }
    }

}