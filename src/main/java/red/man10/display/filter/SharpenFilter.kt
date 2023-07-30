package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

const val DEFAULT_SHARPEN_LEVEL = 1.0

class SharpenFilter(private val strength: Double = DEFAULT_SHARPEN_LEVEL) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val g = resultImage.createGraphics()
        g.drawImage(image, 0, 0, null)

        // 鮮明化フィルタを適用
        val kernel = arrayOf(
            intArrayOf(0, -1, 0),
            intArrayOf(-1, 5, -1),
            intArrayOf(0, -1, 0)
        )

        val kernelSize = kernel.size
        val kernelRadius = kernelSize / 2

        for (y in kernelRadius until height - kernelRadius) {
            for (x in kernelRadius until width - kernelRadius) {
                var sumRed = 0
                var sumGreen = 0
                var sumBlue = 0

                for (ky in 0 until kernelSize) {
                    for (kx in 0 until kernelSize) {
                        val pixel = image.getRGB(x + kx - kernelRadius, y + ky - kernelRadius)
                        val weight = kernel[ky][kx]

                        val red = (pixel shr 16) and 0xFF
                        val green = (pixel shr 8) and 0xFF
                        val blue = pixel and 0xFF

                        sumRed += red * weight
                        sumGreen += green * weight
                        sumBlue += blue * weight
                    }
                }

                var newRed = (sumRed * strength).toInt()
                var newGreen = (sumGreen * strength).toInt()
                var newBlue = (sumBlue * strength).toInt()

                newRed = newRed.coerceIn(0, 255)
                newGreen = newGreen.coerceIn(0, 255)
                newBlue = newBlue.coerceIn(0, 255)

                val newColor = Color(newRed, newGreen, newBlue)
                resultImage.setRGB(x, y, newColor.rgb)
            }
        }

        g.dispose()

        return resultImage
    }
}
