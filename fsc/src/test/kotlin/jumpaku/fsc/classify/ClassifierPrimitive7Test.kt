package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.json.parseJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassifierPrimitive7Test {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/classify/")

    @Test
    fun testClassify() {
        println("ClassifierPrimitive.classify")
        for (i in (0..9)) {
            val s = path.resolve("fsc$i.json").toFile().readText().parseJson().get().bSpline
            val e = path.resolve("primitiveResult$i.json").toFile().readText().parseJson().get().classifyResult
            val (eClass, eGrade) = e
            val (aClass, aGrade) = ClassifierPrimitive7(nSamples = 25, nFmps = 15).classify(s)
            assertThat(aClass).`as`("$i").isEqualTo(eClass)
            assertThat(aGrade).`as`("$i").isEqualTo(eGrade)
        }
    }
}
