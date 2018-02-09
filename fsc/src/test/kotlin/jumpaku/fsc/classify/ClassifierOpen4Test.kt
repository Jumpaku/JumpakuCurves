package jumpaku.fsc.classify

import com.github.salomonbrys.kotson.array
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.json.parseToJson
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassifierOpen4Test {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/classify/")

    @Test
    fun testClassify() {
        println("ClassifierOpen4.Classify")
        val fscs = path.resolve("Fscs.json").toFile().readText().parseToJson().get().array.map { it.bSpline }
        val es = path.resolve("Open4Results.json").toFile().readText().parseToJson().get().array.map { it.classifyResult }
        assertThat(fscs.size).isEqualTo(7)
        assertThat(es.size).isEqualTo(7)
        fscs.zip(es).forEachIndexed { i, (s, e) ->
            val (eClass, eGrade) = e
            val (aClass, aGrade) = ClassifierOpen4(nSamples = 25, nFmps = 15).classify(s)
            assertThat(aClass).`as`("$i").isEqualTo(eClass)
            assertThat(aGrade).`as`("$i").isEqualTo(eGrade)
        }
    }
}
