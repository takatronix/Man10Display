import org.bukkit.configuration.file.YamlConfiguration
import red.man10.display.Display

class ImageDisplay(name: String, width: Int, height: Int) : Display<Any?>(name,width,height) {
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
