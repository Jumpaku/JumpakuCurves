package jumpaku.curves.experimental.fsc.test.edit

import jumpaku.commons.test.matcher
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.experimental.fsc.edit.Editor
import jumpaku.curves.experimental.fsc.edit.FscComponent
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import org.hamcrest.TypeSafeMatcher
import org.junit.Test


fun isCloseTo(actual: FscComponent, expected: FscComponent, error: Double = 1.0e-9): Boolean =
        actual.keys == expected.keys && actual.keys.all { key ->
            isCloseTo(actual[key]!!, expected[key]!!, error)
        }

fun closeTo(expected: FscComponent, precision: Double = 1.0e-9): TypeSafeMatcher<FscComponent> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }



class EditorTest {

    @Test
    fun testEditor() {
        println("Editor")

    }
}


object Settings {

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
    val blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.5,
            threshold = Grade.FALSE)
    val blendGenerator = BlendGenerator(
            degree = generator.degree,
            knotSpan = generator.knotSpan,
            bandWidth = blender.samplingSpan,
            extendInnerSpan = generator.extendInnerSpan,
            extendOuterSpan = generator.extendOuterSpan,
            extendDegree = generator.extendDegree,
            fuzzifier = generator.fuzzifier)
    val fragmenter = Fragmenter(
            threshold = Chunk.Threshold(
                    necessity = 0.4,
                    possibility = 0.7),
            chunkSize = 8,
            minStayTimeSpan = 0.05)
    val editor: Editor = Editor(
            nConnectorSamples = 17,
            connectionThreshold = Grade.FALSE,
            blender = { exist, overlap -> blender.blend(exist, overlap).map { blendGenerator.generate(it) } },
            fragmenter = { merged -> fragmenter.fragment(merged) })

}
