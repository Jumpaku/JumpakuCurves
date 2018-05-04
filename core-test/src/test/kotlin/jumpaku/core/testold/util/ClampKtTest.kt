package jumpaku.core.testold.util

import jumpaku.core.util.clamp
import org.assertj.core.api.Assertions.*
import org.junit.Test

class ClampKtTest {

    @Test
    fun testClamp() {
        println("Clamp")
        assertThat(clamp(-2, -1, 2)).isEqualTo(-1)
        assertThat(clamp(-1, -1, 2)).isEqualTo(-1)
        assertThat(clamp(0, -1, 2)).isEqualTo(0)
        assertThat(clamp(1, -1, 2)).isEqualTo(1)
        assertThat(clamp(2, -1, 2)).isEqualTo(2)
        assertThat(clamp(3, -1, 2)).isEqualTo(2)
    }

}