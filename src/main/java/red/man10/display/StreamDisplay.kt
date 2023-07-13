package red.man10.display

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import java.awt.image.BufferedImage
import java.util.function.Consumer
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis


class StreamDisplay: Display {
    private var port: Int = 0
    private var videoCaptureServer = VideoCaptureServer(port)

    constructor(name: String, width: Int, height: Int, port: Int) : super(name, width, height){
        this.port = port
        startServer()
    }
    constructor(config: YamlConfiguration, name:String) : super(config,name){
        this.load(config,name)
        startServer()
    }

    private fun startServer(){
        videoCaptureServer.onFrame(Consumer { image ->
            this.bufferedImage = image
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

    fun drawInformation(){
        var info = getInfo()
        // infoを出力
        for text in info



        var y = 20
        val step = 20
        drawText("SentMapCount:$sentMapCount",0,y, color = 0x00ff00)
        y += step
        drawText("lastCacheTime:$lastCacheTime(ms)",0,y, color = 0x00ff00)

        y += step
        drawText("frameReceivedCount:${videoCaptureServer.frameReceivedCount}",0,y, color = 0x00ff00)
        y += step
        drawText("frameErrorCount:${videoCaptureServer.frameErrorCount}",0,y, color = 0x00ff00)


    }
}
