package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

const val DEFAULT_DENOISE_RADIUS = 2

class DenoiseFilter(private val radius: Int = DEFAULT_DENOISE_RADIUS) : ImageFilter() {
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

        // 平滑化フィルタを適用
        val tempImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        applyConvolution(image, tempImage, weights, kernelSize)

        // 結果をクランプして出力画像に設定
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = tempImage.getRGB(x, y)
                resultImage.setRGB(x, y, rgb)
            }
        }

        return resultImage
    }

    private fun applyConvolution(
        source: BufferedImage,
        destination: BufferedImage,
        weights: FloatArray,
        kernelSize: Int
    ) {
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
                                val color = Color(pixel)
                                rSum += color.red * weight
                                gSum += color.green * weight
                                bSum += color.blue * weight
                            }
                        }
                    }
                }

                val r = rSum.toInt().coerceIn(0, 255)
                val g = gSum.toInt().coerceIn(0, 255)
                val b = bSum.toInt().coerceIn(0, 255)
                val rgb = Color(r, g, b).rgb
                resultPixels[y * width + x] = rgb
            }
        }

        destination.setRGB(0, 0, width, height, resultPixels, 0, width)
    }
}
