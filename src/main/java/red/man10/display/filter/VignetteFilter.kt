package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.pow


const val DEFAULT_VIGNETTE_LEVEL = 1.0

/*
strength = 0.5: 標準的な効果。周辺光量がわずかに暗くなりますが、比較的自然な印象を与えます。
strength = 1.0: 強い効果。周辺光量がかなり暗くなり、中心の主題が強調されます。
strength = 0.2: ソフトな効果。周辺光量が軽く暗くなり、中心の主題がやや強調されます。
*/
class VignetteFilter(private val strength: Double = DEFAULT_VIGNETTE_LEVEL) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        if (strength <= 0.0) {
            return image // strengthが0以下の場合は元の画像を返す
        }

        val width = image.width
        val height = image.height
        val centerX = width / 2.0
        val centerY = height / 2.0
        val maxDistance = Math.sqrt(centerX.pow(2) + centerY.pow(2))

        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = resultImage.createGraphics()
        g.drawImage(image, 0, 0, null)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val distanceX = x - centerX
                val distanceY = y - centerY
                val distance = Math.sqrt(distanceX.pow(2) + distanceY.pow(2))
                val fraction = (distance / maxDistance).pow(strength)
                val color = Color(image.getRGB(x, y))

                val red = (color.red * (1 - fraction)).toInt().coerceIn(0, 255)
                val green = (color.green * (1 - fraction)).toInt().coerceIn(0, 255)
                val blue = (color.blue * (1 - fraction)).toInt().coerceIn(0, 255)

                val newColor = Color(red, green, blue)
                resultImage.setRGB(x, y, newColor.rgb)
            }
        }

        g.dispose()

        return resultImage
    }
}