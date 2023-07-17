package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

class ColorQuantizeFilter(private val levels: Int = 16) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        // カラーパレットの生成
        val palette = generatePalette(levels)

        // 画像の各ピクセルをカラーパレットに量子化する
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = image.getRGB(x, y)
                val color = Color(rgb)
                val quantizedColor = findClosestColor(color, palette)
                resultImage.setRGB(x, y, quantizedColor.rgb)
            }
        }

        return resultImage
    }

    private fun generatePalette(levels: Int): List<Color> {
        val palette = mutableListOf<Color>()
        val step = 255 / (levels - 1)

        for (i in 0 until levels) {
            val intensity = i * step
            val color = Color(intensity, intensity, intensity)
            palette.add(color)
        }

        return palette
    }

    private fun findClosestColor(color: Color, palette: List<Color>): Color {
        var closestColor = palette[0]
        var minDistance = colorDistance(color, closestColor)

        for (i in 1 until palette.size) {
            val distance = colorDistance(color, palette[i])
            if (distance < minDistance) {
                closestColor = palette[i]
                minDistance = distance
            }
        }

        return closestColor
    }

}
