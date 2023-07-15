package red.man10.display.imageprocessor

import java.awt.Color
import java.awt.image.BufferedImage

class GrayscaleProcessor : ImageProcessor() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        // グレースケール化
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = Color(image.getRGB(x, y))
                val red = pixel.red
                val green = pixel.green
                val blue = pixel.blue
                val gray = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
                val newColor = Color(gray, gray, gray)
                result.setRGB(x, y, newColor.rgb)
            }
        }

        return result
    }
}
