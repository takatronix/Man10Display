package red.man10.display.filter

import java.awt.image.BufferedImage

/*
threshold: しきい値
低いしきい値（例: 50〜100）: エッジが強調され、より多くのエッジが検出されます。ノイズが増える可能性もあります。
中程度のしきい値（例: 100〜200）: 標準的なエッジ検出のバランスが取れた結果が得られます。
高いしきい値（例: 200〜255）: エッジの検出が厳しくなり、より明確なエッジのみが残ります。細かい特徴が欠落する可能性があります。
 */

const val DEFAULT_SOBEL_LEVEL = 100

class SobelFilter(private val threshold: Int = DEFAULT_SOBEL_LEVEL) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val grayscaleImage = convertToGrayscale(image)
        val gradientMagnitude = calculateGradientMagnitude(grayscaleImage)
        val thresholdedGradient = applyThreshold(gradientMagnitude)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelValue = thresholdedGradient[x][y].toInt() and 0xFF
                val rgb = (pixelValue shl 16) or (pixelValue shl 8) or pixelValue
                resultImage.setRGB(x, y, rgb)
            }
        }

        return resultImage
    }

    private fun convertToGrayscale(image: BufferedImage): BufferedImage {
        val grayscaleImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
        val g = grayscaleImage.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return grayscaleImage
    }

    private fun calculateGradientMagnitude(image: BufferedImage): Array<FloatArray> {
        val width = image.width
        val height = image.height
        val gradientX = Array(width) { FloatArray(height) }
        val gradientY = Array(width) { FloatArray(height) }
        val gradientMagnitude = Array(width) { FloatArray(height) }

        val sobelX = arrayOf(intArrayOf(-1, 0, 1), intArrayOf(-2, 0, 2), intArrayOf(-1, 0, 1))
        val sobelY = arrayOf(intArrayOf(-1, -2, -1), intArrayOf(0, 0, 0), intArrayOf(1, 2, 1))

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var sumX = 0f
                var sumY = 0f

                for (i in -1..1) {
                    for (j in -1..1) {
                        val pixel = image.getRGB(x + i, y + j)
                        val intensity = getIntensity(pixel)

                        sumX += intensity * sobelX[i + 1][j + 1]
                        sumY += intensity * sobelY[i + 1][j + 1]
                    }
                }

                val magnitude = Math.sqrt((sumX * sumX + sumY * sumY).toDouble())
                val magnitudeNormalized = if (magnitude != 0.0) magnitude else 1.0 // 0で割るのを避ける
                gradientX[x][y] = (sumX / magnitudeNormalized).toFloat()
                gradientY[x][y] = (sumY / magnitudeNormalized).toFloat()
                gradientMagnitude[x][y] = magnitude.toFloat()
            }
        }

        return gradientMagnitude
    }

    private fun applyThreshold(gradientMagnitude: Array<FloatArray>): Array<FloatArray> {
        val width = gradientMagnitude.size
        val height = gradientMagnitude[0].size
        val thresholdedGradient = Array(width) { FloatArray(height) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                thresholdedGradient[x][y] = if (gradientMagnitude[x][y] >= threshold) 255f else 0f
            }
        }

        return thresholdedGradient
    }

    private fun getIntensity(rgb: Int): Float {
        val red = (rgb shr 16) and 0xFF
        val green = (rgb shr 8) and 0xFF
        val blue = rgb and 0xFF
        return (red + green + blue) / 3f
    }
}
