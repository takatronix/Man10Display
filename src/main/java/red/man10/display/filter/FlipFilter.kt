package red.man10.display.filter

import java.awt.image.BufferedImage

class FlipFilter : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val flippedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)

        val g = flippedImage.createGraphics()
        g.drawImage(image, width, 0, -width, height, null)
        g.dispose()

        return flippedImage
    }
}