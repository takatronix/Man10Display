package red.man10.display

import org.bukkit.configuration.file.YamlConfiguration
import java.util.function.Consumer


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
            this.frameReceivedCount = videoCaptureServer?.frameReceivedCount ?: 0
            this.frameReceivedBytes = videoCaptureServer?.frameReceivedBytes ?: 0
            this.frameErrorCount = videoCaptureServer?.frameErrorCount ?: 0

            this.bufferedImage = filterImage(image)
            this.updateMapCache()
            this.mapCacheToPackets()
            this.refreshFlag = true
        })
        videoCaptureServer?.start()
    }

    override fun save(config: YamlConfiguration, key: String) {
        super.save(config, key)
        config.set("$key.port", port)
    }

    override fun load(config: YamlConfiguration, key: String) {
        super.load(config, key)
        port = config.getInt("$key.port")
    }


}
