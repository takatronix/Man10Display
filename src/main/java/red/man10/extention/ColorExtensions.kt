package red.man10.extention

import java.awt.Color

fun Color.toHexRGB(): String {
    val r = this.red.toString(16).padStart(2, '0')
    val g = this.green.toString(16).padStart(2, '0')
    val b = this.blue.toString(16).padStart(2, '0')
    return "#$r$g$b"
}