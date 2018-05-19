package jumpaku.fsc.test.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.ClassifyResult
import org.amshove.kluent.shouldBe
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassifierOpen4Test {

    val urlString = "/jumpaku/fsc/test/classify/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val classifier = ClassifierOpen4(nSamples = 25, nFmps = 15)
    @Test
    fun testClassify() {
        println("ClassifierOpen4.classify")
        for (i in (0..9)) {
            val s = resourceText("fsc$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val e = resourceText("openResult$i.json").parseJson().flatMap { ClassifyResult.fromJson(it) }.get()
            val a = classifier.classify(s)
            a.curveClass.shouldBe(e.curveClass)
        }
    }
}