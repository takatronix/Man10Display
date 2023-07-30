package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.pow

const val DEFAULT_QUANTIZE_LEVEL = 6

class ColorQuantizeFilter(private val levels: Int = DEFAULT_QUANTIZE_LEVEL) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        // Generate the color palette
        val palette = generatePalette(levels)

        // Quantize each pixel of the image to the color palette
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = image.getRGB(x, y)
                val color = Color(rgb)
                val quantizedColor = findClosestColor(color, palette)
                resultImage.setRGB(x, y, quantizedColor.rgb)
            }
        }

        return resultImage
    }

    private fun generatePalette(levels: Int): List<Color> {
        val palette = mutableListOf<Color>()
        val step = 255 / (levels - 1)

        for (r in 0 until levels) {
            for (g in 0 until levels) {
                for (b in 0 until levels) {
                    val color = Color(r * step, g * step, b * step)
                    palette.add(color)
                }
            }
        }

        return palette
    }

    private fun findClosestColor(color: Color, palette: List<Color>): Color {
        var closestColor = palette[0]
        var minDistance = colorDistance(color, closestColor)

        for (i in 1 until palette.size) {
            val distance = colorDistance(color, palette[i])
            if (distance < minDistance) {
                closestColor = palette[i]
                minDistance = distance
            }
        }

        return closestColor
    }

    private fun colorDistance(c1: Color, c2: Color): Double {
        val rDiff = c1.red - c2.red
        val gDiff = c1.green - c2.green
        val bDiff = c1.blue - c2.blue

        return Math.sqrt(rDiff.toDouble().pow(2.0) + gDiff.toDouble().pow(2.0) + bDiff.toDouble().pow(2.0))
    }
}
