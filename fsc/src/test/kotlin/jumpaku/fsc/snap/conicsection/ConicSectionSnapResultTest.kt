package jumpaku.fsc.snap.conicsection

import jumpaku.core.affine.pointAssertThat
import jumpaku.core.curve.rationalbezier.conicSectionAssertThat
import jumpaku.core.json.parseJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.snap.point.pointSnapResultAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.junit.Test
import java.nio.file.Paths


fun conicSectionSnapResultAssertThat(actual: ConicSectionSnapResult): ConicSectionSnapResultAssert = ConicSectionSnapResultAssert(actual)

class ConicSectionSnapResultAssert(actual: ConicSectionSnapResult) : AbstractAssert<ConicSectionSnapResultAssert, ConicSectionSnapResult>(actual, ConicSectionSnapResultAssert::class.java) {

    fun isEqualToConicSectionSnapResult(expected: ConicSectionSnapResult, eps: Double = 1.0e-10): ConicSectionSnapResultAssert {
        isNotNull

        Assertions.assertThat(actual.grade.value).isEqualTo(expected.grade.value, withPrecision(eps))

        assertThat(actual.candidate.featurePoints.size()).isEqualTo(expected.candidate.featurePoints.size())
        actual.candidate.featurePoints.zip(expected.candidate.featurePoints).forEach { (a, e) ->
            pointAssertThat(a.cursor).isEqualToPoint(e.cursor)
            pointSnapResultAssertThat(a.snapped).isEqualToPointSnapResult(e.snapped)
        }
        conicSectionAssertThat(actual.candidate.snappedConicSection).isEqualConicSection(expected.candidate.snappedConicSection)

        val acs = actual.candidates.toArray()
        val ecs = expected.candidates.toArray()
        assertThat(acs.size()).isEqualTo(ecs.size())
        actual.candidates.zip(expected.candidates).forEach { (a, e) ->
            a.featurePoints.zip(e.featurePoints).forEach { (af, ef) ->
                pointAssertThat(af.cursor).isEqualToPoint(ef.cursor)
                pointSnapResultAssertThat(af.snapped).isEqualToPointSnapResult(ef.snapped)
            }
            conicSectionAssertThat(a.snappedConicSection).isEqualConicSection(e.snappedConicSection)
        }

        return this
    }
}

class ConicSectionSnapResultTest {

    val path = Paths.get("./src/test/resources/jumpaku/fsc/snap/conicsection/")

    @Test
    fun testToString() {
        println("ToString")
        for (i in 0..4) {
            val e = path.resolve("ConicSectionSnapResult$i.json").parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            val a = e.toString().parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            conicSectionSnapResultAssertThat(a).`as`("$i").isEqualToConicSectionSnapResult(e)
        }
    }

}