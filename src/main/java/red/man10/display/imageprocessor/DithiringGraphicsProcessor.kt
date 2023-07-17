package red.man10.display.imageprocessor

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class DitheringGraphicsProcessor : ImageProcessor() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height

        val ditheredImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = ditheredImage.graphics as Graphics2D
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val originalColor = Color(image.getRGB(x, y))
                val mappedColor = mapToPalette(originalColor)
                graphics.color = mappedColor
                graphics.fillRect(x, y, 1, 1)
            }
        }

        graphics.dispose()

        return ditheredImage
    }

    private fun mapToPalette(color: Color): Color {
        var minDistance = Int.MAX_VALUE
        var bestMatch = palette[0]

        for (paletteColor in palette) {
            val distance = calculateColorDistance(color, paletteColor)
            if (distance < minDistance) {
                minDistance = distance
                bestMatch = paletteColor
            }
        }

        return bestMatch
    }

    private fun calculateColorDistance(color1: Color, color2: Color): Int {
        val redDiff = color1.red - color2.red
        val greenDiff = color1.green - color2.green
        val blueDiff = color1.blue - color2.blue
        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff
    }
}
