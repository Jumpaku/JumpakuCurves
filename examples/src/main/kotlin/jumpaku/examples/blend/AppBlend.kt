package jumpaku.examples.blend

import com.github.salomonbrys.kotson.jsonArray
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.parseJson
import jumpaku.core.util.Option
import jumpaku.core.util.none
import jumpaku.core.util.some
import jumpaku.core.util.toJson
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.blend.OverlapMatrix
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.nodes.cubicFsc
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.onCurveDone
import tornadofx.*
import java.nio.file.Paths
import kotlin.system.measureNanoTime

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View() {

    val generator = FscGenerator(
            degree = 3,
            knotSpan = 0.1)

    val blender = Blender(
            1.0/128,
            minPossibility = Grade(1e-10),
            blendingRate = 0.5) { path, _ -> path.grade.value }

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0

            /*val path = Paths.get("./fsc-test/src/test/resources/jumpaku/fsc/test/blend")
            for (i in 0..4) {
                val e = path.resolve("BlendExisting$i.json").parseJson().tryFlatMap { BSpline.fromJson(it) }.orThrow()
                val o = path.resolve("BlendOverlapping$i.json").parseJson().tryFlatMap { BSpline.fromJson(it) }.orThrow()
                val result = blender.blend(e, o)
                println(result.map { jsonArray(it.map { it.toJson() }) }.toJson().toString())
                path.resolve("BlendResult$i.json").toFile().apply { createNewFile() }.writeText(result.map { jsonArray(it.map { it.toJson() }) }.toJson().toString())
                //existing = some(e)
                //group.update(o)
            }*/
            onCurveDone { e ->
                clear()
                group.update(FscGenerator().generate(e.data))
            }
        }
    }

    var existing: Option<BSpline> = none()

    fun Group.update(overlap: BSpline){
        children.clear()
        existing.forEach { cubicFsc(it) { stroke = Color.RED } }
        cubicFsc(overlap) { stroke = Color.BLUE }
        existing.forEach {
            //val osm = OverlapMatrix.create(it.sample(blender.samplingSpan).map { it.point },
            //        overlap.sample(blender.samplingSpan).map { it.point })
            /*for (i in 0..osm.rowLastIndex){
                for (j in 0..osm.columnLastIndex) {
                    circle(j + 5, i + 5, 0.5) { fill = Color.hsb(0.0, osm[i, j].value, 1.0) }
                }
            }
            blender.findPaths(osm, Grade(1e-10)).sortedBy { it.grade }.forEach {
                it.forEach { (i, j) ->
                    circle(j + 5, i + 5, 0.5) { fill = Color.hsb(180.0, it.grade.value, 1.0) }
                }
            }*/
            blender.blend(it, overlap).forEach { data ->
                val s = generator.generate(data)
                cubicFsc(s) { stroke = Color.GREEN }
                existing = some(s)
            }
        }
        existing = if (existing.isEmpty) some(overlap) else existing
    }
}
