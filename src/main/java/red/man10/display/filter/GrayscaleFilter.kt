package red.man10.display.filter

import java.awt.image.BufferedImage

class GrayscaleFilter : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val result = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
        val g = result.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return result
    }
}
