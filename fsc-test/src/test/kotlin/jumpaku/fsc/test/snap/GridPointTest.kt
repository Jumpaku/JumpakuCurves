package jumpaku.fsc.test.snap

import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.GridPoint
import org.amshove.kluent.shouldEqual
import org.junit.Test

class GridPointTest {

    val gp = GridPoint(4, -3, 0)

    @Test
    fun testToString() {
        println("ToString")
        gp.toString().parseJson().flatMap { GridPoint.fromJson(it) }.get().shouldEqual(gp)
    }
}