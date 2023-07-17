package red.man10.display.imageprocessor

import java.awt.Color
import java.awt.image.BufferedImage

class ErrorDiffusionDitheringProcessor : ImageProcessor() {
    private val diffusionMatrix = arrayOf(
        intArrayOf(0, 0, 0, 7, 5),
        intArrayOf(3, 5, 7, 5, 3),
        intArrayOf(1, 3, 5, 3, 1)
    )

    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val ditheredImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val originalColor = Color(image.getRGB(x, y))
                val ditheredColor = applyDithering(originalColor)
                ditheredImage.setRGB(x, y, ditheredColor.rgb)

                val errorR = originalColor.red - ditheredColor.red
                val errorG = originalColor.green - ditheredColor.green
                val errorB = originalColor.blue - ditheredColor.blue

                diffuseError(image, x, y, errorR, errorG, errorB)
            }
        }

        return ditheredImage
    }

    private fun applyDithering(color: Color): Color {
        val r = if (color.red >= 128) 255 else 0
        val g = if (color.green >= 128) 255 else 0
        val b = if (color.blue >= 128) 255 else 0
        return Color(r, g, b)
    }

    private fun diffuseError(image: BufferedImage, x: Int, y: Int, errorR: Int, errorG: Int, errorB: Int) {
        for (i in 0..2) {
            for (j in 0..4) {
                val errorFactor = diffusionMatrix[i][j] / 16.0

                if (x + j - 2 >= 0 && x + j - 2 < image.width && y + i < image.height) {
                    val pixel = Color(image.getRGB(x + j - 2, y + i))

                    val r = (pixel.red + errorR * errorFactor).coerceIn(0.0, 255.0)
                    val g = (pixel.green + errorG * errorFactor).coerceIn(0.0, 255.0)
                    val b = (pixel.blue + errorB * errorFactor).coerceIn(0.0, 255.0)

                    val newPixel = Color(r.toInt(), g.toInt(), b.toInt())
                    image.setRGB(x + j - 2, y + i, newPixel.rgb)
                }
            }
        }
    }
}
