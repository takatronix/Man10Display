package red.man10.display.filter

import java.awt.image.BufferedImage
import kotlin.random.Random

/*
amplitude = 1 〜 3: 軽いノイズ効果を得たい場合。画像にわずかな横方向のゆがみを追加します。
amplitude = 5 〜 10: 中程度のノイズ効果を得たい場合。横方向に明確なラスタノイズを加えます。
amplitude = 20 以上: 強いノイズ効果を得たい場合。横方向に大きくゆがんだラスタノイズを加えます。
 */
const val DEFAULT_RASTER_NOISE_LEVEL = 3

class RasterNoiseFilter(private val amplitude: Int = DEFAULT_RASTER_NOISE_LEVEL) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val g = resultImage.createGraphics()
        g.drawImage(image, 0, 0, null)

        val random = Random(System.currentTimeMillis())

        // 横方向にラスタノイズを追加
        for (y in 0 until height) {
            val noiseX = random.nextInt(-amplitude, amplitude + 1)
            for (x in 0 until width) {
                val newX = (x + noiseX).coerceIn(0, width - 1)
                val rgb = image.getRGB(newX, y)
                resultImage.setRGB(x, y, rgb)
            }
        }

        g.dispose()

        return resultImage
    }
}
