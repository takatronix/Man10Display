package red.man10.display.filter

import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage

abstract class ImageFilter {
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

        fun clamp(value: Int): Int {
            return value.coerceIn(0, 255)
        }

        fun colorDistance(color1: Color, color2: Color): Double {
            val rDiff = color1.red - color2.red
            val gDiff = color1.green - color2.green
            val bDiff = color1.blue - color2.blue
            return Math.hypot(rDiff.toDouble(), Math.hypot(gDiff.toDouble(), bDiff.toDouble()))
        }

        fun colorDiff(color1: Color, color2: Color): Int {
            val rDiff = Math.abs(color1.red - color2.red)
            val gDiff = Math.abs(color1.green - color2.green)
            val bDiff = Math.abs(color1.blue - color2.blue)
            return rDiff + gDiff + bDiff
        }

        // 画像をリサイズするメソッド
        fun resizeImage(image: BufferedImage, newWidth: Int, newHeight: Int): BufferedImage {
            val resizedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
            val g = resizedImage.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            g.drawImage(image, 0, 0, newWidth, newHeight, null)
            g.dispose()
            return resizedImage
        }
    }
}