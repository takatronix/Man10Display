package red.man10.display.imageprocessor

import java.awt.image.BufferedImage

class FlipProcessor : ImageProcessor() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val flippedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val newX = width - x - 1        // 左右反転したx座標
                val rgb = image.getRGB(x, y)        // 元画像のピクセルのRGB値
                flippedImage.setRGB(newX, y, rgb) // 反転した座標にRGB値を設定
            }
        }
        return flippedImage
    }
}