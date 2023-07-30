package red.man10.display.filter

import java.awt.image.BufferedImage

class InvertFilter : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height

        val invertedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val srcData = image.getRGB(0, 0, width, height, null, 0, width)
        val destData = IntArray(width * height)

        for (i in srcData.indices) {
            val rgb = srcData[i]
            val r = 255 - (rgb shr 16 and 0xFF)
            val g = 255 - (rgb shr 8 and 0xFF)
            val b = 255 - (rgb and 0xFF)
            destData[i] = 0xFF shl 24 or (r shl 16) or (g shl 8) or b
        }

        invertedImage.setRGB(0, 0, width, height, destData, 0, width)

        return invertedImage
    }
}