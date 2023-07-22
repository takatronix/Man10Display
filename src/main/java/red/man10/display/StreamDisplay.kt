package red.man10.display

import org.bukkit.configuration.file.YamlConfiguration
import java.util.function.Consumer


class StreamDisplay : Display {
    private var videoCaptureServer: VideoCaptureServer? = null

    init{
        if(port != 0)
            videoCaptureServer = VideoCaptureServer(port)
    }
    override fun deinit(){
        super.deinit()
        videoCaptureServer?.deinit()
        videoCaptureServer = null
    }
    fun resetVideoStats() {
        videoCaptureServer?.resetStats()
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

}
