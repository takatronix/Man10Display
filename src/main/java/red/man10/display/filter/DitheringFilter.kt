package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

open class DitheringFilter : ImageFilter() {

    override fun apply(image: BufferedImage): BufferedImage {
        val ditheredImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val rgb = image.getRGB(x, y)
                val originalColor = Color(rgb)
                val mappedColor = mapToPalette(originalColor)
                val error = Color(
                    clamp(originalColor.red - mappedColor.red),
                    clamp(originalColor.green - mappedColor.green),
                    clamp(originalColor.blue - mappedColor.blue)
                )
                ditheredImage.setRGB(x, y, mappedColor.rgb)
                propagateError(image, x, y, error)
            }
        }

        return ditheredImage
    }

    private fun calculateColorDistance(color1: Color, color2: Color): Double {
        val redDiff = color1.red - color2.red
        val greenDiff = color1.green - color2.green
        val blueDiff = color1.blue - color2.blue
        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff.toDouble()
    }

    fun mapToPalette(color: Color): Color {
        var minDistance = Double.MAX_VALUE
        var closestColor = palette[0]

        for (paletteColor in palette) {
            val distance = calculateColorDistance(color, paletteColor)

            if (distance < minDistance) {
                minDistance = distance
                closestColor = paletteColor
            }
        }

        return closestColor
    }

    open fun propagateError(image: BufferedImage, x: Int, y: Int, error: Color) {
        if (x + 1 < image.width) {
            modifyPixel(image, x + 1, y, error, 7.0 / 16)
        }

        if (x - 1 >= 0 && y + 1 < image.height) {
            modifyPixel(image, x - 1, y + 1, error, 3.0 / 16)
        }

        if (y + 1 < image.height) {
            modifyPixel(image, x, y + 1, error, 5.0 / 16)
        }

        if (x + 1 < image.width && y + 1 < image.height) {
            modifyPixel(image, x + 1, y + 1, error, 1.0 / 16)
        }
    }

    private fun modifyPixel(image: BufferedImage, x: Int, y: Int, error: Color, weight: Double) {
        val pixelRgb = image.getRGB(x, y)
        val pixelColor = Color(pixelRgb)
        val modifiedColor = Color(
            clamp(pixelColor.red + (error.red * weight).toInt()),
            clamp(pixelColor.green + (error.green * weight).toInt()),
            clamp(pixelColor.blue + (error.blue * weight).toInt())
        )
        image.setRGB(x, y, modifiedColor.rgb)
    }
}
