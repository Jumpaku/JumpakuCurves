package jumpaku.examples.blend

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.util.Option
import jumpaku.core.util.none
import jumpaku.core.util.some
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.nodes.cubicFsc
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.onCurveDone
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View() {

    val generator = FscGenerator(
            degree = 3,
            knotSpan = 0.1)

    val blender = Blender(
            1.0/128,
            0.5) { _, _, _ -> grade.value }

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
                //println(result.map { jsonArray(it.map { it.toJson() }) }.toJson().toString())
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
            blender.blend(it, overlap).forEach {
                val s = generator.generate(it)
                cubicFsc(s) { stroke = Color.GREEN }
                existing = some(s)
            }
        }
        existing = if (existing.isEmpty) some(overlap) else existing
    }
}
