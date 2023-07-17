package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

class CartoonFilter(private val levels: Int = 6, private val sobelLevel: Int = 100) : ImageFilter() {
    private val colorQuantizeFilter = ColorQuantizeFilter(levels)
    private val sobelFilter = SobelFilter(sobelLevel)

    override fun apply(image: BufferedImage): BufferedImage {
        val quantizedImage = colorQuantizeFilter.apply(image)
        val edges = sobelFilter.apply(image)

        return blendImages(quantizedImage, edges)
    }

    private fun blendImages(image: BufferedImage, edges: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = Color(image.getRGB(x, y))
                val edgeColor = Color(edges.getRGB(x, y))

                val r = Math.max(0, color.red - edgeColor.red)
                val g = Math.max(0, color.green - edgeColor.green)
                val b = Math.max(0, color.blue - edgeColor.blue)

                val newColor = Color(r, g, b)
                resultImage.setRGB(x, y, newColor.rgb)
            }
        }

        return resultImage
    }
}
