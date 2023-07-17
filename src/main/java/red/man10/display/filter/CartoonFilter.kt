package red.man10.display.filter

import java.awt.Color
import java.awt.image.BufferedImage

/*
edgeThreshold: エッジのしきい値を指定します。
これはエッジと判断するための明るさのしきい値で、明るさがこの値以上のピクセルはエッジとして扱われます。
エッジの強度やエッジの数を調整するために使用します。適切な値は画像に依存し、試行錯誤で調整する必要があります。一般的な範囲は 0 〜 255 です。

colorLevels: カラーの量子化レベルを指定します。
これは画像のカラーパレットの色の数を制限するために使用されます。
値が大きいほど、より多くの色が保持されますが、カートゥーン効果が弱くなります。値が小さいほど、色の数が減り、よりフラットなカートゥーン効果が得られます。
適切な値は画像に依存し、試行錯誤で調整する必要があります。一般的な範囲は 2 〜 256 です。
 */

class CartoonFilter(private val edgeThreshold: Int = 100, private val colorLevels: Int=100) : ImageFilter() {
    override fun apply(image: BufferedImage): BufferedImage {
        val grayscaleImage = convertToGrayscale(image)
        val edgeImage = detectEdges(grayscaleImage)
        val quantizedImage = quantizeColors(image, colorLevels)
        val resultImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val edgeColor = Color(edgeImage.getRGB(x, y))
                val quantizedColor = Color(quantizedImage.getRGB(x, y))
                val cartoonColor = if (isEdgeColor(edgeColor, edgeThreshold)) {
                    edgeColor
                } else {
                    quantizedColor
                }
                resultImage.setRGB(x, y, cartoonColor.rgb)
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

    private fun detectEdges(image: BufferedImage): BufferedImage {
        val edgeFilter = SobelFilter(edgeThreshold)
        return edgeFilter.apply(image)
    }

    private fun quantizeColors(image: BufferedImage, levels: Int): BufferedImage {
        val colorQuantizeFilter = ColorQuantizeFilter(levels)
        return colorQuantizeFilter.apply(image)
    }

    private fun isEdgeColor(color: Color, threshold: Int): Boolean {
        val brightness = (color.red + color.green + color.blue) / 3
        return brightness >= threshold
    }
}
