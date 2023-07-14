package red.man10.display.imageprocessor

import org.bukkit.Color
import java.awt.image.BufferedImage


class DitheringProcessor : ImageProcessor() {

    private val palette = MapPalette.colors

    override fun apply(image: BufferedImage): BufferedImage {
        val ditheredImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val originalColor = Color.fromRGB(image.getRGB(x, y))
                val mappedColor = mapToPalette(originalColor)
                val error = Color.fromRGB(
                    originalColor.red - mappedColor.red,
                    originalColor.green - mappedColor.green,
                    originalColor.blue - mappedColor.blue
                )

                ditheredImage.setRGB(x, y, mappedColor.asRGB())

                // Error diffusion. Adjust neighboring pixels as per Floyd-Steinberg Dithering
                if (x + 1 < image.width) {
                    adjustPixelColor(image, x + 1, y, error, 7.0 / 16.0)
                }
                if (y + 1 < image.height) {
                    if (x - 1 >= 0) {
                        adjustPixelColor(image, x - 1, y + 1, error, 3.0 / 16.0)
                    }
                    adjustPixelColor(image, x, y + 1, error, 5.0 / 16.0)
                    if (x + 1 < image.width) {
                        adjustPixelColor(image, x + 1, y + 1, error, 1.0 / 16.0)
                    }
                }
            }
        }

        return ditheredImage
    }

    private fun mapToPalette(color: Color): Color {
        var closestColor = palette[0]
        var closestDistance = Double.MAX_VALUE

        for (paletteColor in palette) {
            val distance = colorDistance(color, paletteColor)
            if (distance < closestDistance) {
                closestColor = paletteColor
                closestDistance = distance
            }
        }

        return closestColor
    }

    private fun colorDistance(c1: Color, c2: Color): Double {
        val rDiff = c1.red - c2.red
        val gDiff = c1.green - c2.green
        val bDiff = c1.blue - c2.blue
        return Math.sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble())
    }

    private fun adjustPixelColor(image: BufferedImage, x: Int, y: Int, error: Color, factor: Double) {
        val originalColor = Color.fromRGB(image.getRGB(x, y))
        val newColor = Color.fromRGB(
            clip(originalColor.red + (error.red * factor).toInt()),
            clip(originalColor.green + (error.green * factor).toInt()),
            clip(originalColor.blue + (error.blue * factor).toInt())
        )
        image.setRGB(x, y, newColor.asRGB())
    }

    private fun clip(value: Int, min: Int = 0, max: Int = 255): Int {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
}
