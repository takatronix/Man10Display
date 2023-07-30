package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

const val DEFAULT_BRIGHTNESS_LEVEL = 1.5

class BrightnessFilter(private val brightness: Double = DEFAULT_BRIGHTNESS_LEVEL) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val g = resultImage.createGraphics()
        g.drawImage(image, 0, 0, null)

        // ブライトネスを調整
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = image.getRGB(x, y)
                val color = Color(rgb)

                val newRed = (color.red * brightness).toInt().coerceIn(0, 255)
                val newGreen = (color.green * brightness).toInt().coerceIn(0, 255)
                val newBlue = (color.blue * brightness).toInt().coerceIn(0, 255)

                val newColor = Color(newRed, newGreen, newBlue)
                resultImage.setRGB(x, y, newColor.rgb)
            }
        }

        g.dispose()

        return resultImage
    }
}
