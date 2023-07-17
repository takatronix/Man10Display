package red.man10.display.filter

import java.awt.image.BufferedImage

class SepiaFilter : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val sepiaImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val srcData = image.getRGB(0, 0, width, height, null, 0, width)
        val destData = IntArray(width * height)

        for (i in srcData.indices) {
            val rgb = srcData[i]
            val r = (rgb shr 16 and 0xFF)
            val g = (rgb shr 8 and 0xFF)
            val b = (rgb and 0xFF)

            val newR = (0.393 * r + 0.769 * g + 0.189 * b).toInt().coerceAtMost(255)
            val newG = (0.349 * r + 0.686 * g + 0.168 * b).toInt().coerceAtMost(255)
            val newB = (0.272 * r + 0.534 * g + 0.131 * b).toInt().coerceAtMost(255)

            destData[i] = (newR shl 16) or (newG shl 8) or newB
        }

        sepiaImage.setRGB(0, 0, width, height, destData, 0, width)

        return sepiaImage
    }
}