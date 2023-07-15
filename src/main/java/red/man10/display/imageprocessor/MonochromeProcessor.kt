package red.man10.display.imageprocessor

import java.awt.Color
import java.awt.image.BufferedImage

class MonochromeProcessor : ImageProcessor() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val threshold = 128
        val monochromeImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = Color(image.getRGB(x, y))
                val red = pixel.red
                val green = pixel.green
                val blue = pixel.blue
                val gray = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()

                val binaryColor = if (gray < threshold) Color.BLACK else Color.WHITE

                monochromeImage.setRGB(x, y, binaryColor.rgb)
            }
        }

        return monochromeImage
    }
}
