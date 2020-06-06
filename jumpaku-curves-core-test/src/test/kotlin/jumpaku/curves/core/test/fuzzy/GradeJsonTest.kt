package jumpaku.curves.core.test.fuzzy

import jumpaku.commons.json.parseJson
import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.fuzzy.GradeJson
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class GradeJsonTest {

    @Test
    fun testGradeJson() {
        println("GradeJson")
        val a = GradeJson.toJsonStr(Grade(1.0)).parseJson().let { GradeJson.fromJson(it) }
        val b = GradeJson.toJsonStr(Grade(0.0)).parseJson().let { GradeJson.fromJson(it) }
        val c = GradeJson.toJsonStr(Grade(0.5)).parseJson().let { GradeJson.fromJson(it) }
        Assert.assertThat(a.value, CoreMatchers.`is`(closeTo(Grade(1.0).value)))
        Assert.assertThat(b.value, CoreMatchers.`is`(closeTo(Grade(0.0).value)))
        Assert.assertThat(c.value, CoreMatchers.`is`(closeTo(Grade(0.5).value)))
    }

}