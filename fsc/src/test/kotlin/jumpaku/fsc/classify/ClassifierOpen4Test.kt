package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.json.parseToJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassifierOpen4Test {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/classify/")

    @Test
    fun testClassify() {
        println("ClassifierOpen4.classify")
        for (i in (0..9)) {
            val s = path.resolve("fsc$i.json").toFile().readText().parseToJson().get().bSpline
            val e = path.resolve("openResult$i.json").toFile().readText().parseToJson().get().classifyResult
            val (eClass, eGrade) = e
            val (aClass, aGrade) = ClassifierOpen4(nSamples = 25, nFmps = 15).classify(s)
            assertThat(aClass).`as`("$i").isEqualTo(eClass)
            assertThat(aGrade).`as`("$i").isEqualTo(eGrade)
        }
    }
}
