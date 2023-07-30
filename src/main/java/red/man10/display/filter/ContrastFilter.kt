package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

const val DEFAULT_CONTRAST_LEVEL = 1.3

class ContrastFilter(private val factor: Double = DEFAULT_CONTRAST_LEVEL) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = image.getRGB(x, y)
                val color = Color(rgb)

                // RGBチャンネルごとにコントラストを調整
                val newRed = adjustContrast(color.red, factor)
                val newGreen = adjustContrast(color.green, factor)
                val newBlue = adjustContrast(color.blue, factor)

                // 新しいRGB値でピクセルを設定
                val newColor = Color(newRed, newGreen, newBlue)
                resultImage.setRGB(x, y, newColor.rgb)
            }
        }

        return resultImage
    }

    private fun adjustContrast(value: Int, factor: Double): Int {
        val newValue = (value.toFloat() - 128) * factor + 128
        return newValue.toInt().coerceIn(0, 255)
    }
}
