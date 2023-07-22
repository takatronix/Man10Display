package red.man10.display

import org.bukkit.configuration.file.YamlConfiguration
import java.util.function.Consumer

class ImageDisplay : Display {

    init{
    }
    override fun deinit(){
        super.deinit()
    }
    constructor(name: String, width: Int, height: Int,programName:String) : super(name, width, height) {
        info("aa")
    }

    constructor(config: YamlConfiguration, name: String) : super(config, name) {
    }

}