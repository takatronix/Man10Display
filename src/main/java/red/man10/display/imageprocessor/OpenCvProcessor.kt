package red.man10.display.imageprocessor

import org.bytedeco.javacpp.Loader
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.image.BufferedImage


open class OpenCvProcessor : ImageProcessor() {
    override fun apply(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    }
    open fun matToBufferedImage(mat: Mat?): BufferedImage {
        return Java2DFrameUtils.toBufferedImage(mat)
    }

    open fun bufferedImageToMat(image: BufferedImage?): Mat? {
        return Java2DFrameUtils.toMat(image)
    }
    fun gray(image:BufferedImage): BufferedImage{
        val result = Mat()
        opencv_imgproc.cvtColor(bufferedImageToMat(image), result, opencv_imgproc.COLOR_BGR2GRAY)
        return matToBufferedImage(result)
    }

}
