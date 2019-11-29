package jumpaku.curves.fsc.test.merge

import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.merge.MergeData
import jumpaku.curves.fsc.merge.OverlapDetector
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test

class OverlapDetectorTest {

    val urlString = "/jumpaku/curves/fsc/test/merge/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val samplingSpan = 0.01
    val threshold = Grade(1e-10)
    val mergeRate = 0.5
    val overlapDetector = OverlapDetector(overlapThreshold = Grade(1e-10))

    @Test
    fun testDetect() {
        println("Detect")
        for (i in 0..4) {
            val existing = resourceText("BlendExisting$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val overlapping = resourceText("BlendOverlapping$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val eSampled = existing.sample(samplingSpan)
            val aSampled = overlapping.sample(samplingSpan)
            val actual = overlapDetector.detect(eSampled, aSampled).map {
                MergeData.parameterize(eSampled, aSampled, mergeRate, it)
            }
            val expected = resourceText("BlendDataOpt$i.json")
                    .parseJson().value().flatMap { Option.fromJson(it).map { MergeData.fromJson(it) } }
            assertThat(actual.isDefined, `is`(expected.isDefined))
            if (actual is Some) {
                assertThat(actual.value.grade, `is`(greaterThan(threshold)))
                assertThat(actual.orThrow(), `is`(closeTo(expected.orThrow())))
            }
        }
    }
}
