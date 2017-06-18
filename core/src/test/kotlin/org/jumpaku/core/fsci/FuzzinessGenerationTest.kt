package org.jumpaku.core.fsci

import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Vector
import org.junit.Test


class FuzzinessGenerationTest {
    @Test
    fun testGenerateFuzziness() {
        println("GenerateFuzziness")
        val vCoff = 0.004
        val aCoff = 0.003
        assertThat(generateFuzziness(Vector(1.0, -2.0, 2.0), Vector(0.0, 0.0, 100.0))).isEqualTo(0.312, withPrecision(1.0e-10))
        assertThat(generateFuzziness(Vector(1.0, -2.0, 2.0), Vector(0.0, 0.0, 100.0))).isGreaterThanOrEqualTo(0.0)
    }

}