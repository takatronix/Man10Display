package red.man10.display.filter

import java.awt.image.BufferedImage
import java.util.*

//  intensity = 0.05 // グリッチの強度（0から1の範囲で指定）
class NoiseFilter(private val intensity: Double = 0.05) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val glitchedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val srcData = image.getRGB(0, 0, width, height, null, 0, width)
        val destData = srcData.copyOf()

        val random = Random()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x

                if (random.nextDouble() < intensity) {
                    val randomX = random.nextInt(width)
                    val randomY = random.nextInt(height)
                    val randomIndex = randomY * width + randomX

                    val temp = destData[index]
                    destData[index] = destData[randomIndex]
                    destData[randomIndex] = temp
                }
            }
        }

        glitchedImage.setRGB(0, 0, width, height, destData, 0, width)

        return glitchedImage
    }
}
