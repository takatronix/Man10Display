package red.man10.display

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ConfigData {
    var imagePath: String = ""
    var appMapId: Int = 0
    fun save(config: FileConfiguration) {
        config.set("imagePath", imagePath)
        config.set("appMapId", appMapId)

    }

    fun load(plugin: JavaPlugin, config: FileConfiguration) {
        val defaultPath = plugin.dataFolder.path + File.separator + "saved" + File.separator
        this.imagePath = config.getString("imagePath", defaultPath) ?: defaultPath
        this.appMapId = config.getInt("appMapId", 0)
    }
}