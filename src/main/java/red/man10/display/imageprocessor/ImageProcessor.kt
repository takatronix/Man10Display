package red.man10.display.imageprocessor

import java.awt.Color
import java.awt.image.BufferedImage

abstract class ImageProcessor {
    abstract fun apply(image: BufferedImage): BufferedImage

    companion object {
        val palette: List<Color> = createPaletteFromMapPalette()
        private fun createPaletteFromMapPalette(): List<Color> {
            val mapPaletteColors = getMapPaletteColors()
            val palette = mutableListOf<Color>()

            for (color in mapPaletteColors) {
                val rgb = color.rgb
                val red = rgb shr 16 and 0xFF
                val green = rgb shr 8 and 0xFF
                val blue = rgb and 0xFF
                val paletteColor = Color(red, green, blue)
                palette.add(paletteColor)
            }

            return palette
        }

        private fun getMapPaletteColors(): Array<Color> {
            try {
                val mapPaletteClass = Class.forName("org.bukkit.map.MapPalette")
                val colorsField = mapPaletteClass.getDeclaredField("colors")
                colorsField.isAccessible = true
                return colorsField.get(null) as Array<Color>
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return emptyArray()
        }
    }
}