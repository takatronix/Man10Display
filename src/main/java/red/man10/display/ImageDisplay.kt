package red.man10.display

import org.bukkit.configuration.file.YamlConfiguration

class ImageDisplay : Display {
    init {
    }

    constructor(name: String, width: Int, height: Int) : super(name, width, height)

    constructor(config: YamlConfiguration, name: String) : super(config, name)
}