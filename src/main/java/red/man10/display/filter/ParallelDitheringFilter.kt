package red.man10.display.filter

import kotlinx.coroutines.*
import java.awt.Color
import java.awt.image.BufferedImage

const val DEFAULT_PARALLELISM = 4

class ParallelDitheringFilter(private val parallelism: Int = DEFAULT_PARALLELISM) : DitheringFilter() {
    @OptIn(ObsoleteCoroutinesApi::class)
    private val context = newFixedThreadPoolContext(parallelism, "bgPool")

    override fun apply(image: BufferedImage): BufferedImage = runBlocking {
        val ditheredImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)

        val jobs = List(image.height) { y ->
            launch(context) {
                for (x in 0 until image.width) {
                    val rgb = image.getRGB(x, y)
                    val originalColor = Color(rgb)
                    val mappedColor = mapToPalette(originalColor)
                    val error = Color(
                        clamp(originalColor.red - mappedColor.red),
                        clamp(originalColor.green - mappedColor.green),
                        clamp(originalColor.blue - mappedColor.blue)
                    )
                    ditheredImage.setRGB(x, y, mappedColor.rgb)
                    propagateError(image, x, y, error)
                }
            }
        }
        jobs.joinAll()
        return@runBlocking ditheredImage
    }
}
