package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.fuzzy.TruthValue
import jumpaku.core.json.parseToJson
import jumpaku.core.json.prettyGson
import org.assertj.core.api.Assertions
import org.junit.Test

import java.nio.file.Paths

class ChunkTest {

    private val eps = 1.0e-5
    private val threshold = TruthValue(0.4, 0.6)
    private val path = Paths.get("./src/test/resources/jumpaku/fsc/fragment/")
    private val dataFile = path.resolve("FragmenterTestFsc0.json").toFile()
    private val fsc = dataFile.readText().parseToJson().get().bSpline

    @Test
    fun state() {
        println("State")
        val c1 = chunk(fsc, Interval(106535.12613733625, 106535.20095651352), 4)
        Assertions.assertThat(c1.state(threshold)).isEqualTo(Chunk.State.MOVE)
        val c2 = chunk(fsc, Interval(106535.45035377113, 106535.52517294842), 4)
        Assertions.assertThat(c2.state(threshold)).isEqualTo(Chunk.State.STAY)
        val c3 = chunk(fsc, Interval(106535.55011267416, 106535.62493185145), 4)
        Assertions.assertThat(c3.state(threshold)).isEqualTo(Chunk.State.UNKNOWN)
    }

    @Test
    fun getNecessity() {
        val c = chunk(fsc, Interval(106535.55011267416, 106535.62493185145), 4)
        Assertions.assertThat(c.necessity.value).isEqualTo(0.042337862693934136, Assertions.withPrecision(eps))
    }

    @Test
    fun getPossibility() {
        val c = chunk(fsc, Interval(106535.55011267416, 106535.62493185145), 4)
        Assertions.assertThat(c.possibility.value).isEqualTo(0.8946264226651022, Assertions.withPrecision(eps))
    }

}