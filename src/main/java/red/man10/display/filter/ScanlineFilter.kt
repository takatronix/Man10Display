package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

const val DEFAULT_SCANLINE_HEIGHT = 2

class ScanlineFilter(
    private val scanlineHeight: Int = DEFAULT_SCANLINE_HEIGHT,
    private val scanlineColor: Color = Color.BLACK
) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        // 元の画像をコピー
        val g = resultImage.graphics
        g.drawImage(image, 0, 0, null)

        // スキャンラインを追加
        g.color = scanlineColor
        for (y in 0 until height) {
            if (y % scanlineHeight == 0) {
                g.fillRect(0, y, width, 1)
            }
        }
        g.dispose()

        return resultImage
    }
}
