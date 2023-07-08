package red.man10.display

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.awt.image.BufferedImage
import java.util.function.Consumer

interface Savable {
    fun save(config: YamlConfiguration, path: String)
    fun load(config: YamlConfiguration, path: String)
}

open class Display(name: String, width: Int, height: Int) : Savable {
    var name:String = ""
    open var mapIdList = mutableListOf<Int>()
    var width: Int = 1
    var height: Int = 1
    var bufferedImage: BufferedImage? = null

    var location:Location? = null
    init {
        this.name = name
        this.width = width
        this.height = height
        this.bufferedImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
    }


    open fun deinit(){}
    open var modified = false
    val imageWidth: Int
        get() = width * 128
    val imageHeight: Int
        get() = height * 128
    open val mapCount:Int
        get() = width * height

    override fun save(config: YamlConfiguration, path: String) {
        config.set("$path.class", javaClass.name)
        config.set("$path.name", name)
        config.set("$path.mapIdList", mapIdList)
        config.set("$path.width", width)
        config.set("$path.height", height)
        // Location情報を保存
        location?.let { loc ->
            config.set("$path.location.world", loc.world?.name)
            config.set("$path.location.x", loc.x)
            config.set("$path.location.y", loc.y)
            config.set("$path.location.z", loc.z)
            config.set("$path.location.yaw", loc.yaw)
            config.set("$path.location.pitch", loc.pitch)
        }
    }

    override fun load(config: YamlConfiguration, path: String) {
        mapIdList = (config.getIntegerList("$path.mapIdList")  ?: mutableListOf()).toMutableList()
        name = config.getString("$path.name") ?: ""
        width = config.getInt("$path.width")
        height = config.getInt("$path.height")
        // Location情報を読み込む
        val worldName = config.getString("$path.location.world")
        val x = config.getDouble("$path.location.x")
        val y = config.getDouble("$path.location.y")
        val z = config.getDouble("$path.location.z")
        val yaw = config.getDouble("$path.location.yaw").toFloat()
        val pitch = config.getDouble("$path.location.pitch").toFloat()

        if (worldName != null) {
            val world = Bukkit.getWorld(worldName)
            location = Location(world, x, y, z, yaw, pitch)
        }
    }
}

class ImageDisplay(name: String, width: Int, height: Int) : Display(name,width,height) {
    var imageUrl = ""

    override fun save(config: YamlConfiguration, path: String) {
        super.save(config, path)
        config.set("$path.imageUrl", imageUrl)
    }

    override fun load(config: YamlConfiguration, path: String) {
        super.load(config, path)
        imageUrl = config.getString("$path.imageUrl") ?: ""
    }
}

class StreamDisplay(name: String, width: Int, height: Int, port: Int) : Display(name,width,height){
    private var port: Int = 0
    private var videoCaptureServer = VideoCaptureServer(port)

    init {
        this.port = port
        videoCaptureServer.onFrame(Consumer { bufferedImage ->
            this.bufferedImage = bufferedImage
            modified = true
        })
        videoCaptureServer.start()
    }
    override fun deinit() {
        videoCaptureServer.deinit()
    }

    override fun save(config: YamlConfiguration, path: String) {
        super.save(config, path)
        config.set("$path.port", port)
    }

    override fun load(config: YamlConfiguration, path: String) {
        super.load(config, path)
        port = config.getInt("$path.port")
    }
}
