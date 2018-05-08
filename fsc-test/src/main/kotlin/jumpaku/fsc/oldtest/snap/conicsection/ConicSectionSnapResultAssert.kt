package jumpaku.fsc.oldtest.snap.conicsection

import jumpaku.core.testold.affine.pointAssertThat
import jumpaku.core.testold.curve.rationalbezier.conicSectionAssertThat
import jumpaku.fsc.snap.conicsection.ConicSectionSnapResult
import jumpaku.fsc.oldtest.snap.point.pointSnapResultAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun conicSectionSnapResultAssertThat(actual: ConicSectionSnapResult): ConicSectionSnapResultAssert = ConicSectionSnapResultAssert(actual)
class ConicSectionSnapResultAssert(actual: ConicSectionSnapResult) : AbstractAssert<ConicSectionSnapResultAssert, ConicSectionSnapResult>(actual, ConicSectionSnapResultAssert::class.java) {

    fun isEqualToConicSectionSnapResult(expected: ConicSectionSnapResult, eps: Double = 1.0e-10): ConicSectionSnapResultAssert {
        isNotNull

        Assertions.assertThat(actual.grade.value).isEqualTo(expected.grade.value, Assertions.withPrecision(eps))

        Assertions.assertThat(actual.candidate.featurePoints.size()).isEqualTo(expected.candidate.featurePoints.size())
        actual.candidate.featurePoints.zip(expected.candidate.featurePoints).forEach { (a, e) ->
            pointAssertThat(a.cursor).isEqualToPoint(e.cursor)
            pointSnapResultAssertThat(a.snapped).isEqualToPointSnapResult(e.snapped)
        }
        conicSectionAssertThat(actual.candidate.snappedConicSection).isEqualConicSection(expected.candidate.snappedConicSection)

        val acs = actual.candidates.toArray()
        val ecs = expected.candidates.toArray()
        Assertions.assertThat(acs.size()).isEqualTo(ecs.size())
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