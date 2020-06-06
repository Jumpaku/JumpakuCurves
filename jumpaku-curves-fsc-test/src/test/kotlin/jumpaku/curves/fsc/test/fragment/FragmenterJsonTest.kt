package jumpaku.curves.fsc.test.fragment

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.fragment.FragmenterJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class FragmenterJsonTest{

    val threshold = Chunk.Threshold(0.4, 0.6)
    val fragmenter = Fragmenter(threshold, 4, 0.1)

    @Test
    fun testFragmenterJson() {
        println("FragmenterJson")
        val a = FragmenterJson.toJsonStr(fragmenter).parseJson().let { FragmenterJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(closeTo(fragmenter)))
    }

}