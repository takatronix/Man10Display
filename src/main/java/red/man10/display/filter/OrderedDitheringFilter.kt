package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

class OrderedDitheringFilter : ImageFilter() {
    private val ditherMatrix = arrayOf(
        arrayOf(0, 48, 12, 60, 3, 51, 15, 63),
        arrayOf(32, 16, 44, 28, 35, 19, 47, 31),
        arrayOf(8, 56, 4, 52, 11, 59, 7, 55),
        arrayOf(40, 24, 36, 20, 43, 27, 39, 23),
        arrayOf(2, 50, 14, 62, 1, 49, 13, 61),
        arrayOf(34, 18, 46, 30, 33, 17, 45, 29),
        arrayOf(10, 58, 6, 54, 9, 57, 5, 53),
        arrayOf(42, 26, 38, 22, 41, 25, 37, 21)
    )

    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val ditheredImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val originalColor = Color(image.getRGB(x, y))
                val ditheredColor = applyDithering(originalColor, x, y)
                ditheredImage.setRGB(x, y, ditheredColor.rgb)
            }
        }

        return ditheredImage
    }

    private fun applyDithering(color: Color, x: Int, y: Int): Color {
        val r = applyDitheringToChannel(color.red, x, y)
        val g = applyDitheringToChannel(color.green, x, y)
        val b = applyDitheringToChannel(color.blue, x, y)
        return Color(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    private fun applyDitheringToChannel(channelValue: Int, x: Int, y: Int): Int {
        val clampedValue = channelValue.coerceIn(0, 255)  // 色の値を範囲内にクランプする
        val ditherValue = ditherMatrix[x % 8][y % 8]
        val quantizedChannelValue = (clampedValue / 64) * 64
        return if (clampedValue % 64 > ditherValue) quantizedChannelValue + 64 else quantizedChannelValue
    }
}
