package red.man10.extention

import java.awt.Color
import org.bukkit.Color as BukkitColor
import java.awt.Color as AwtColor

fun Color.toHexRGB(): String {
    val r = this.red.toString(16).padStart(2, '0')
    val g = this.green.toString(16).padStart(2, '0')
    val b = this.blue.toString(16).padStart(2, '0')
    return "#$r$g$b"
}


// org.bukkit.Color から java.awt.Color への変換
fun BukkitColor.toAwtColor(): AwtColor {
    return AwtColor(this.red, this.green, this.blue)
}

// java.awt.Color から org.bukkit.Color への変換
fun AwtColor.toBukkitColor(): BukkitColor {
    return BukkitColor.fromRGB(this.red, this.green, this.blue)
}