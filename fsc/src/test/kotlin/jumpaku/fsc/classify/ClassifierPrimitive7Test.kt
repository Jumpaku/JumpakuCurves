package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.json.parseToJson
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassifierPrimitive7Test {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/classify/")

    @Test
    fun testClassify() {
        println("ClassifierPrimitive7.Classify")
        (0..6).forEach { i ->
            val fsc = path.resolve("Fsc$i.json").toFile().readText().parseToJson().get().bSpline
            val (eClass, eGrade) = path.resolve("Primitive7Result$i.json").toFile().readText().parseToJson().get().classifyResult
            val (aClass, aGrade) = ClassifierPrimitive7().classify(fsc)
            assertThat(aClass).`as`("$i").isEqualTo(eClass)
            assertThat(aGrade.value).isEqualTo(eGrade.value, withPrecision(1.0e-10))
        }
    }
}
