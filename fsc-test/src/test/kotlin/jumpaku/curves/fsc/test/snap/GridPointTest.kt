package jumpaku.curves.fsc.test.snap

import jumpaku.curves.core.json.parseJson
import jumpaku.curves.fsc.snap.GridPoint
import org.amshove.kluent.shouldEqual
import org.junit.Test

class GridPointTest {

    val gp = GridPoint(4, -3, 0)

    @Test
    fun testToString() {
        println("ToString")
        gp.toString().parseJson().tryFlatMap { GridPoint.fromJson(it) }.orThrow().shouldEqual(gp)
    }
}