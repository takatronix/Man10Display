package red.man10.display.imageprocessor

import java.awt.image.BufferedImage

abstract class ImageProcessor {
    abstract fun apply(image: BufferedImage): BufferedImage
}