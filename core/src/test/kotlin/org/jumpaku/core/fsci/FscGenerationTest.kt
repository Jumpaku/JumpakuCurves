package org.jumpaku.core.fsci

import org.assertj.core.api.Assertions.fail
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class FscGenerationTest {

    val path: Path = Paths.get(".").resolve("src/test/kotlin/org/jumpaku/core/fsci")

    @Test
    fun testGenerate() {
        println("Generate")
        println(path.toFile().exists())
        fail("Generate not implemented.")
    }

    @Test
    fun testCreateFuzzinessDataVector() {
        println("CreateFuzzinessDataVector")
        fail("CreateFuzzinessDataVector not implemented.")
    }


}