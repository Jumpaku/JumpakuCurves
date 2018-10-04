package jumpaku.fxcomponents.colors

import javafx.scene.paint.Paint


data class Color(val red: Double, val green: Double, val blue: Double, val opacity: Double) {

    companion object {

        fun rgb(red: Int, green: Int, blue: Int, opacity: Double = 1.0): Color = Color(
                (red/255.0).coerceIn(0.0..1.0),
                (green/255.0).coerceIn(0.0..1.0),
                (blue/255.0).coerceIn(0.0..1.0),
                opacity
        )
    }
}

fun Color.fx(): Paint = javafx.scene.paint.Color(red, green, blue, opacity)

object CudPalette {

    // neutral colors
    val WHITE: Color = Color.rgb(255, 255, 255)
    val LIGHT_GRAY: Color = Color.rgb(200, 200, 203)
    val DARK_GRAY: Color = Color.rgb(127, 135, 143)
    val BLACK: Color = Color.rgb(0, 0, 0)

    // accent colors
    val RED: Color = Color.rgb(255, 40, 0)
    val YELLOW: Color = Color.rgb(250, 245, 0)
    val GREEN: Color = Color.rgb(53, 161, 107)
    val BLUE: Color = Color.rgb(0, 65, 255)
    val SKY: Color = Color.rgb(102, 204, 255)
    val PINK: Color = Color.rgb(255, 153, 160)
    val ORANGE: Color = Color.rgb(255, 153, 0)
    val PURPLE: Color = Color.rgb(154, 0, 121)
    val BROWN: Color = Color.rgb(102, 51, 0)

    // base colors
    val LIGHT_PINK: Color = Color.rgb(255, 209, 209)
    val CREAM: Color = Color.rgb(255, 255, 153)
    val LIGHT_YELLOW_GREEN: Color = Color.rgb(203, 242, 102)
    val LIGHT_SKY: Color = Color.rgb(180, 235, 250)
    val BEIGE: Color = Color.rgb(237, 197, 143)
    val LIGHT_GREEN: Color = Color.rgb(135, 231, 176)
    val LIGHT_PURPLE: Color = Color.rgb(199, 178, 222)
}