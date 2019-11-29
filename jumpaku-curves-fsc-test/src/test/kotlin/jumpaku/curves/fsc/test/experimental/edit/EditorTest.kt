package jumpaku.curves.fsc.test.experimental.edit

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.merge.Merger
import jumpaku.curves.fsc.experimental.edit.Editor
import jumpaku.curves.fsc.experimental.edit.FscGraph
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class EditorTest {

    val urlString = "/jumpaku/curves/fsc/test/experimental/edit/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val nFscs = 35

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.025,
            extendInnerSpan = 0.15,
            extendOuterSpan = 0.15,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.0085,
                    accelerationCoefficient = 0.007
            )
    )
    val merger = Merger(
            degree = generator.degree,
            knotSpan = generator.knotSpan,
            extendDegree = generator.extendDegree,
            extendInnerSpan = generator.extendInnerSpan,
            extendOuterSpan = generator.extendOuterSpan,
            samplingSpan = 0.01,
            overlapThreshold = Grade.FALSE,
            mergeRate = 0.5,
            bandWidth = 0.01,
            fuzzifier = generator.fuzzifier)
    val fragmenter = Fragmenter(
            threshold = Chunk.Threshold(
                    necessity = 0.45,
                    possibility = 0.75),
            chunkSize = 4,
            minStayTimeSpan = 0.05)
    val editor: Editor = Editor(
            nConnectorSamples = 17,
            connectionThreshold = Grade.FALSE,
            merger = merger,
            fragmenter = fragmenter)

    @Test
    fun testEditor() {
        println("Editor")
        var a = FscGraph.of()
        for (i in 0 until nFscs) {
            val s = resourceText("EditingFsc$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            a = editor.edit(a, s)
            val e = resourceText("EditedFscGraph$i.json").parseJson().tryMap { FscGraph.fromJson(it) }.orThrow()
            assertThat(a, `is`(closeTo(e, 1e-10)))
        }
    }
}
