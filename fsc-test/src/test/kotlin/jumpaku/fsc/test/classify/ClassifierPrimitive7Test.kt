package jumpaku.fsc.test.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.classify.ClassifierPrimitive7
import jumpaku.fsc.classify.ClassifyResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassifierPrimitive7Test {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/test/classify/")

    @Test
    fun testClassify() {
        println("ClassifierPrimitive.classify")
        for (i in (0..9)) {
            val s = path.resolve("fsc$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val e = path.resolve("primitiveResult$i.json").parseJson().flatMap { ClassifyResult.fromJson(it) }.get()
            val (eClass, eGrade) = e
            val (aClass, aGrade) = ClassifierPrimitive7(nSamples = 25, nFmps = 15).classify(s)
            assertThat(aClass).`as`("$i").isEqualTo(eClass)
            assertThat(aGrade).`as`("$i").isEqualTo(eGrade)
        }
    }
}
