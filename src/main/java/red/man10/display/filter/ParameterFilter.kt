package red.man10.display.filter

import java.awt.image.BufferedImage


class ParameterFilter(private val parameter: String) : ImageFilter() {

    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height

        val kv = parameter.split(":")
        val key = kv[0]

        when (key) {
            "dithering" -> return DitheringFilter().apply(image)
            "invert" -> return InvertFilter().apply(image)
            "grayscale" -> return GrayscaleFilter().apply(image)
            "sepia" -> return SepiaFilter().apply(image)
            "brightness" -> return BrightnessFilter(kv[1].toDouble()).apply(image)
            "contrast" -> return ContrastFilter(kv[1].toDouble()).apply(image)
            "noise" -> return NoiseFilter(kv[1].toDouble()).apply(image)
            "raster" -> return RasterNoiseFilter(kv[1].toDouble().toInt()).apply(image)
        }
        return image
    }
}
