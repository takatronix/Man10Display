package red.man10.display.imageprocessor

import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class AspectRatioProcessor(private val aspectRatio: Double) : ImageProcessor() {
    override fun apply(image: BufferedImage): BufferedImage {
        val originalWidth = image.width
        val originalHeight = image.height

        val targetWidth: Int
        val targetHeight: Int

        if (originalWidth / aspectRatio <= originalHeight) {
            targetWidth = originalWidth
            targetHeight = (originalWidth / aspectRatio).toInt()
        } else {
            targetWidth = (originalHeight * aspectRatio).toInt()
            targetHeight = originalHeight
        }

        val processedImage = BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = processedImage.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

        val x = (originalWidth - targetWidth) / 2
        val y = (originalHeight - targetHeight) / 2

        g2d.drawImage(image, x, y, targetWidth, targetHeight, null)
        g2d.dispose()

        return processedImage
    }
}
