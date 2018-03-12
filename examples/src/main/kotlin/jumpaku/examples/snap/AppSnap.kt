package jumpaku.examples.snap

import javafx.application.Application
import javafx.scene.layout.Pane
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.fsc.snap.BaseGrid
import jumpaku.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.fsc.snap.point.PointSnapper
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane


fun main(vararg args: String) = Application.launch(AppSnap::class.java, *args)

class AppSnap : App(ViewSnap::class)

class ViewSnap : View() {

    val w = 1280.0

    val h = 720.0

    val conicSectionSnapper = ConicSectionSnapper(
            PointSnapper(
                    BaseGrid(
                            spacing = 50.0,
                            magnification = 2,
                            origin = Point.xy(w/2, h/2),
                            axis = Vector.K,
                            radian = 0.0,
                            fuzziness = 10.0),
                    minResolution = -1,
                    maxResolution = 1),
            ConjugateCombinator())

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = w
            prefHeight = h
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                }
            }
        }
    }
}

