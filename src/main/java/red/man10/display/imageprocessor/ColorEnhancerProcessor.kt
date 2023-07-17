package red.man10.display.imageprocessor

import java.awt.Color
import java.awt.image.BufferedImage

class ColorEnhancerProcessor(private val saturationFactor: Double) : ImageProcessor() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val enhancedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = image.getRGB(x, y)
                val originalColor = Color(rgb)
                val enhancedColor = enhanceColor(originalColor, saturationFactor)
                enhancedImage.setRGB(x, y, enhancedColor.rgb)
            }
        }

        return enhancedImage
    }

    private fun enhanceColor(color: Color, saturationFactor: Double): Color {
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        val hue = hsb[0]
        val saturation = hsb[1]
        val brightness = hsb[2]

        val enhancedSaturation = (saturation * saturationFactor).coerceIn(0.0, 1.0)

        return Color.getHSBColor(hue, enhancedSaturation.toFloat(), brightness)
    }
}
