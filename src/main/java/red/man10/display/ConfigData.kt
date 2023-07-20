package red.man10.display

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ConfigData {
    var imagePath: String = ""
    fun save(config: FileConfiguration) {
        config.set("imagePath", imagePath)
    }
    fun load(plugin: JavaPlugin, config: FileConfiguration) {
        val defaultPath = plugin.dataFolder.path + File.separator +  "images" + File.separator
        this.imagePath = config.getString("imagePath",defaultPath) ?: defaultPath
    }
}