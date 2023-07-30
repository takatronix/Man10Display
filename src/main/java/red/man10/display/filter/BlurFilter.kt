package red.man10.display.filter

import java.awt.image.BufferedImage

const val DEFAULT_BLUR_RADIUS = 3

class BlurFilter(private val radius: Int = DEFAULT_BLUR_RADIUS) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        // カーネルのサイズを計算
        val kernelSize = radius * 2 + 1

        // カーネルの重みを計算
        val weights = FloatArray(kernelSize * kernelSize)
        val weight = 1.0f / (kernelSize * kernelSize)
        for (i in weights.indices) {
            weights[i] = weight
        }

        // 水平方向へのぼかし処理
        val tempImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val horizontalBlurred = applyConvolution(image, tempImage, weights, kernelSize)

        // 垂直方向へのぼかし処理
        val verticalBlurred = applyConvolution(horizontalBlurred, resultImage, weights, kernelSize)

        return verticalBlurred
    }

    private fun applyConvolution(
        source: BufferedImage,
        destination: BufferedImage,
        weights: FloatArray,
        kernelSize: Int
    ): BufferedImage {
        val width = source.width
        val height = source.height

        val pixels = source.getRGB(0, 0, width, height, null, 0, width)
        val resultPixels = IntArray(pixels.size)

        val halfKernelSize = kernelSize / 2

        for (y in 0 until height) {
            for (x in 0 until width) {
                var rSum = 0.0f
                var gSum = 0.0f
                var bSum = 0.0f

                for (ky in -halfKernelSize..halfKernelSize) {
                    val offsetY = y + ky
                    if (offsetY >= 0 && offsetY < height) {
                        for (kx in -halfKernelSize..halfKernelSize) {
                            val offsetX = x + kx
                            if (offsetX >= 0 && offsetX < width) {
                                val weight = weights[(ky + halfKernelSize) * kernelSize + (kx + halfKernelSize)]
                                val pixel = pixels[offsetY * width + offsetX]
                                val r = pixel shr 16 and 0xFF
                                val g = pixel shr 8 and 0xFF
                                val b = pixel and 0xFF

                                rSum += r * weight
                                gSum += g * weight
                                bSum += b * weight
                            }
                        }
                    }
                }

                val r = rSum.toInt().coerceIn(0, 255)
                val g = gSum.toInt().coerceIn(0, 255)
                val b = bSum.toInt().coerceIn(0, 255)

                resultPixels[y * width + x] = 0xFF shl 24 or (r shl 16) or (g shl 8) or b
            }
        }

        destination.setRGB(0, 0, width, height, resultPixels, 0, width)
        return destination
    }
}
