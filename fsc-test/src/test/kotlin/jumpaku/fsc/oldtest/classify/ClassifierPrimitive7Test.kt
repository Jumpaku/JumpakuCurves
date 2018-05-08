package jumpaku.fsc.oldtest.classify

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

    val classifier = ClassifierPrimitive7(nSamples = 25, nFmps = 15)

    @Test
    fun testClassify() {
        println("ClassifierPrimitive7.classify")
        for (i in (0..9)) {
            val s = path.resolve("fsc$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val e = path.resolve("primitiveResult$i.json").parseJson().flatMap { ClassifyResult.fromJson(it) }.get()
            val a = classifier.classify(s)
            assertThat(a.curveClass).`as`("$i").isEqualTo(e.curveClass)
        }
    }
}
