package red.man10.display

import org.bukkit.configuration.file.YamlConfiguration
import red.man10.display.imageprocessor.DitheringProcessor
import red.man10.display.imageprocessor.ImageProcessor
import java.util.function.Consumer


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
            if (this.dithering){
                this.bufferedImage = DitheringProcessor().apply(this.bufferedImage!!)
            }

            this.drawInformation()
            this.updateMapCache()
            this.modified = true
        })
        videoCaptureServer.start()
        info("Server started on port $port")
    }

    override fun deinit() {
        videoCaptureServer.stop()
        super.deinit()
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
        var info = getInfo()
        // infoを出力
        for (i in info.indices) {
            drawText(info[i], 0, 20 + i * 20, color = 0x00ff00)
        }


    }
}
