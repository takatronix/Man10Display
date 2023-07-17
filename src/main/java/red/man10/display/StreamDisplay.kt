package red.man10.display

import red.man10.display.filter.*
import org.bukkit.configuration.file.YamlConfiguration
import java.util.function.Consumer
import kotlin.system.measureTimeMillis


class StreamDisplay : Display<Any?> {
    private var port: Int = 0
    private var videoCaptureServer = VideoCaptureServer(port)

    constructor(name: String, width: Int, height: Int, port: Int) : super(name, width, height) {
        this.port = port
        startServer()
    }

    constructor(config: YamlConfiguration, name: String) : super(config, name) {
        this.load(config, name)
        startServer()
    }

    private fun startServer() {
        videoCaptureServer.onFrame(Consumer { image ->
            this.bufferedImage = image
            this.lastEffectTime = measureTimeMillis {
                if(this.flip) {
                    this.bufferedImage = FlipFilter().apply(this.bufferedImage!!)
                }
                if (this.monochrome) {
                    this.bufferedImage = GrayscaleFilter().apply(this.bufferedImage!!)
                }
                if(this.sepia){
                    this.bufferedImage = SepiaFilter().apply(this.bufferedImage!!)
                }
                if (this.invert) {
                    this.bufferedImage = InvertFilter().apply(this.bufferedImage!!)
                }
                if(this.keepAspectRatio){
                   this.bufferedImage = AspectRatioFilter(this.aspectRatioWidth / this.aspectRatioHeight).apply(this.bufferedImage!!)
                }
                if(this.colorEnhancer){
                    this.bufferedImage = ColorEnhancerFilter(saturationFactor).apply(this.bufferedImage!!)
                }
                if (this.dithering) {
                    this.bufferedImage = DitheringFilter().apply(this.bufferedImage!!)
                }
                if (this.fastDithering) {
                    this.bufferedImage = OrderedDitheringFilter().apply(this.bufferedImage!!)
                }
                if(this.testMode){
                    this.bufferedImage = ColorQuantizeFilter().apply(this.bufferedImage!!)
                }
                if (this.showStatus) {
                    this.drawInformation()
                }
            }

            this.updateMapCache()
            this.modified = true
        })
        videoCaptureServer.start()
        info("Server started on port $port")
    }

    override fun save(config: YamlConfiguration, path: String) {
        super.save(config, path)
        config.set("$path.port", port)
    }

    override fun load(config: YamlConfiguration, name: String) {
        super.load(config, name)
        port = config.getInt("$name.port")
    }

    fun drawInformation() {
        val info = getInfo()
        // infoを出力
        for (i in info.indices) {
            drawText(info[i], 0, 20 + i * 20, color = 0x00ff00)
        }


    }
}
