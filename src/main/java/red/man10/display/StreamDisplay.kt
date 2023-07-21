package red.man10.display

import red.man10.display.filter.*
import org.bukkit.configuration.file.YamlConfiguration
import java.util.function.Consumer
import kotlin.system.measureTimeMillis


class StreamDisplay : Display {
    private var port: Int = 0
    private var videoCaptureServer: VideoCaptureServer? = null

    init{
        videoCaptureServer = VideoCaptureServer(port)
    }
    override fun deinit(){
        super.deinit()
        videoCaptureServer?.deinit()
        videoCaptureServer = null
    }
    constructor(name: String, width: Int, height: Int, port: Int) : super(name, width, height) {
        this.port = port
        startServer()
    }

    constructor(config: YamlConfiguration, name: String) : super(config, name) {
        this.load(config, name)
        startServer()
    }

    private fun startServer() {
        videoCaptureServer?.onFrame(Consumer { image ->
            this.bufferedImage = filterImage(image)
            this.updateMapCache()
            this.mapCacheToPackets()
            this.refreshFlag = true
        })
        videoCaptureServer?.start()
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
