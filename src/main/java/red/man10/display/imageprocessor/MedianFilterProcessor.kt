package red.man10.display.imageprocessor

import java.awt.Color
import java.awt.image.BufferedImage

class MedianFilterProcessor : ImageProcessor() {
    private val radius = 1

    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val filteredImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val neighbors = getNeighbors(x, y, image)
                val medianColor = getMedian(neighbors)
                filteredImage.setRGB(x, y, medianColor.rgb)
            }
        }

        return filteredImage
    }

    private fun getNeighbors(x: Int, y: Int, image: BufferedImage): List<Color> {
        val neighbors = mutableListOf<Color>()
        for (i in -radius..radius) {
            for (j in -radius..radius) {
                val newX = x + i
                val newY = y + j
                if (newX in 0 until image.width && newY in 0 until image.height) {
                    neighbors.add(Color(image.getRGB(newX, newY)))
                }
            }
        }
        return neighbors
    }

    private fun getMedian(colors: List<Color>): Color {
        val rs = colors.map { it.red }.sorted()
        val gs = colors.map { it.green }.sorted()
        val bs = colors.map { it.blue }.sorted()
        return Color(rs[rs.size / 2], gs[gs.size / 2], bs[bs.size / 2])
    }
}
