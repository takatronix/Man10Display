package red.man10.display

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.*

interface Savable {
    fun save(config: YamlConfiguration, path: String)
    fun load(config: YamlConfiguration, path: String)
}

open class Display : Savable {
    var name:String = ""
    var mapIdList = mutableListOf<Int>()
    var width: Int = 1
    var height: Int = 1
    var location:Location? = null

    val imageWidth: Int
        get() = width * 128
    val imageHeight: Int
        get() = height * 128
    val mapCount:Int
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
        mapIdList = (config.getIntegerList("$path.mapIdList") ?: mutableListOf()).toMutableList()
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

class ImageDisplay : Display() {
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

class StreamDisplay : Display() {
    var streamUrl: String = ""
    var udpPort: Int = 0

    override fun save(config: YamlConfiguration, path: String) {
        super.save(config, path)
        config.set("$path.udpPort", udpPort)
    }

    override fun load(config: YamlConfiguration, path: String) {
        super.load(config, path)
        udpPort = config.getInt("$path.udpPort")
    }
}
