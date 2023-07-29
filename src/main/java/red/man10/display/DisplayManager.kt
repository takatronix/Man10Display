package red.man10.display

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.filter.*
import java.io.File


class DisplayManager(main: JavaPlugin)   : Listener {
    val displays = mutableListOf<Display>()

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)
    }

    fun deinit(){
        this.stopAllMacro()
        for (display in displays) {
            if(display is StreamDisplay){
                info("stream display ${display.name} deinit start")
                display.deinit()
                info("stream display ${display.name} deinited")
            }
        }
        displays.clear()
        info("displays cleared")
    }

    val names:ArrayList<String>
        get() {
            val nameList = arrayListOf<String>()
            for (display in displays) {
                nameList.add(display.name)
            }
            return nameList
        }
    val parameterKeys:ArrayList<String>
        get() {
            return arrayListOf(
                "fps","interval","dithering","location","protect",
                "fast_dithering","show_status",
                "monochrome","sepia","invert","flip",
                "saturation_level","color_enhancer",
                "keep_aspect_ratio","aspect_ratio_width", "aspect_ratio_height",
                "noise_level","noise",
                "raster_noise","raster_noise_level",
                "vignette","vignette_level",
                "quantize_level","quantize",
                "sobel_level","sobel",
                "cartoon",
                "blur","blur_radius",
                "denoise","denoise_radius",
                "contrast","contrast_level","brightness","brightness_level",
                "sharpen","sharpen_level",
                "scanline","scanline_height",
                "distance",
                "parallel_dithering","parallelism",
                "test_mode")
        }
    fun getDisplay(name: String): Display? {
        displays.find { it.name == name }?.let {
            return it
        }
        return null
    }

    fun create(player: Player,display: Display) : Boolean{
        if(getDisplay(display.name) != null){
            player.sendMessage(Main.prefix + "§a§l ${display.name} already exists")
            return false
        }
        if(!createMaps(display,player,display.width,display.height)){
            return false
        }
        display.location = player.location
        displays.add(display)
        return true
    }
    fun delete(p: CommandSender, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        display.deinit()
        displays.remove(display)
        return true
    }
    fun getMaps(player: Player, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        return getMaps(display,player)
    }
    fun showList(p: CommandSender): Boolean {
        p.sendMessage(Main.prefix + "§a§l Display List")
        for (display in displays) {
            p.sendMessage("§a§l ${display.name} ${display.className} ${display.width}x${display.height} ${display.locInfo}")
        }
        return true
    }
    fun showInfo(p: CommandSender, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        p.sendMessage(Main.prefix + "§a§l Display Info")
        p.sendMessage("§a§l name: ${display.name}")
        p.sendMessage("§a§l width: ${display.width}")
        p.sendMessage("§a§l height: ${display.height}")
        p.sendMessage("§a§l location: ${display.locInfo}")
        p.sendMessage("§a§l distance: ${display.distance}")
        p.sendMessage("§a§l fps: ${display.fps}")
        p.sendMessage("§a§l protect: ${display.protect}")

        // パラメータを表示
        p.sendMessage("§a§l monochrome: ${display.monochrome}")
        p.sendMessage("§a§l sepia: ${display.sepia}")
        p.sendMessage("§a§l dithering: ${display.dithering}")
        p.sendMessage("§a§l fast_dithering: ${display.fastDithering}")
        p.sendMessage("§a§l show_status: ${display.showStatus}")
        p.sendMessage("§a§l flip: ${display.flip}")
        p.sendMessage("§a§l invert: ${display.invert}")
        p.sendMessage("§a§l saturation_factor: ${display.saturationLevel}")
        p.sendMessage("§a§l color_enhancer: ${display.colorEnhancer}")
        p.sendMessage("§a§l keep_aspect_ratio: ${display.keepAspectRatio}")
        p.sendMessage("§a§l aspect_ratio_width: ${display.aspectRatioWidth}")
        p.sendMessage("§a§l aspect_ratio_height: ${display.aspectRatioHeight}")
        p.sendMessage("§a§l noise: ${display.noise}")
        p.sendMessage("§a§l noise_level: ${display.noiseLevel}")
        p.sendMessage("§a§l quantize: ${display.quantize}")
        p.sendMessage("§a§l quantize_level: ${display.quantizeLevel}")
        p.sendMessage("§a§l sobel: ${display.sobel}")
        p.sendMessage("§a§l sobel_level: ${display.sobelLevel}")
        p.sendMessage("§a§l cartoon: ${display.cartoon}")
        p.sendMessage("§a§l blur: ${display.blur}")
        p.sendMessage("§a§l blur_radius: ${display.blurRadius}")
        p.sendMessage("§a§l denoise: ${display.denoise}")
        p.sendMessage("§a§l denoise_radius: ${display.denoiseRadius}")
        p.sendMessage("§a§l contrast: ${display.contrast}")
        p.sendMessage("§a§l contrast_level: ${display.contrastLevel}")
        p.sendMessage("§a§l sharpen: ${display.sharpen}")
        p.sendMessage("§a§l sharpen_level: ${display.sharpenLevel}")
        p.sendMessage("§a§l scanline: ${display.scanline}")
        p.sendMessage("§a§l scanline_width: ${display.scanlineHeight}")
        p.sendMessage("§a§l brightness: ${display.brightness}")
        p.sendMessage("§a§l brightnessLevel: ${display.brightnessLevel}")
        p.sendMessage("§a§l distance: ${display.distance}")
        p.sendMessage("§a§l parallelDithering: ${display.parallelDithering}")
        p.sendMessage("§a§l parallelism: ${display.parallelism}")
        p.sendMessage("§a§l rasterNoise: ${display.rasterNoise}")
        p.sendMessage("§a§l rasterNoiseLevel: ${display.rasterNoiseLevel}")
        p.sendMessage("§a§l vignette: ${display.vignette}")
        p.sendMessage("§a§l vignetteLevel: ${display.vignetteLevel}")

        p.sendMessage("§a§l test_mode: ${display.testMode}")

        return true
    }
    fun showStats(sender: CommandSender, name: String): Boolean {
        sender.sendMessage(Main.prefix + "§a§l Display Stats")
        val display = getDisplay(name) ?: return false
        val stats = display.getStatistics()
        for(v in stats){
            sender.sendMessage("§a§l $v")
        }

        return true
    }
    private fun createMaps(display:Display, player: Player, xSize:Int, ySize:Int): Boolean {
        for(y in 0 until ySize){
            for(x in 0 until xSize){
                val mapView = Bukkit.getServer().createMap(player.world)
                mapView.scale = MapView.Scale.CLOSEST
                mapView.isUnlimitedTracking = true

                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = mapView

                val name = "${x+1}-${y+1}"
                mapMeta.displayName(Component.text(name))
                itemStack.itemMeta = mapMeta

                player.world.dropItem(player.location,itemStack)
                display.mapIds.add(mapView.id)
                player.sendMessage("$name created")
            }
        }
        return true
    }

    private fun getMaps(display:Display, player: Player): Boolean {
        val items = getMaps(display)
        for(item in items){
            player.world.dropItem(player.location,item)
        }
        return true
    }

    fun getMaps(display: Display): ArrayList<ItemStack> {
        val items = arrayListOf<ItemStack>()
        for(y in 0 until display.height){
            for(x in 0 until display.width){
                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = Bukkit.getMap(display.mapIds[y * display.width + x])

                val name = "${x+1}-${y+1}"
                mapMeta.displayName(Component.text(name))
                itemStack.itemMeta = mapMeta
                items.add(itemStack)
            }
        }
        return items
    }

    fun save(p: CommandSender): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "displays.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        try{
            for(display in displays){
                display.save(config,display.name)
            }
            config.save(file)
        }catch (e:Exception){
            error(e.message!!,p)
            return false
        }
        return true
    }

    fun load(p: CommandSender? = null ): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "displays.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        deinit()
        for (key in config.getKeys(false)) {
            val className = config.getString("$key.class")
            if(className == StreamDisplay::class.simpleName){
                val display = StreamDisplay(config,key)
                displays.add(display)
                continue
            }
            if(className == ImageDisplay::class.simpleName){
                val display = ImageDisplay(config,key)
                displays.add(display)
                continue
            }
        }
        return true
    }

    fun set(sender: CommandSender, displayName: String, key: String, value: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        val ret = display.set(sender,key,value)
        if(ret)
            save(sender)
            display.clearCache()
        return ret
    }
    fun refresh(sender: CommandSender, displayName: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        display.resetStats()
        display.refreshFlag = true
        return  true
    }

    fun stopAll(sender: CommandSender): Boolean {
        for (display in displays) {
            stopMacro(sender,display.name)
        }
        return true
    }

    fun runMacro(sender: CommandSender, displayName: String,macroName: String? = null): Boolean {
        val display = getDisplay(displayName) ?: return false

        if(macroName == null){
            display.macroEngine.stop()
            display.update()
            return false
        }
        display.runMacro(macroName)
        display.resetStats()
        display.refreshFlag = true
        return  true
    }
    fun stopMacro(sender: CommandSender, displayName: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        display.macroEngine.stop()
        display.reset()
        return true
    }
    fun stopAllMacro(): Boolean {
        for (display in displays) {
            display.macroEngine.stop()
            display.update()
        }
        return true
    }
    fun image(sender: CommandSender, displayName: String,path:String): Boolean {
        val display = getDisplay(displayName) ?: return false
        display.image(path)
        return true
    }


    fun showMacroList(sender: CommandSender): Boolean {
        val list = MacroEngine.macroList
        sender.sendMessage(Main.prefix + "§a§l Macro List")
        for (macro in list) {
            sender.sendMessage("§a§l $macro")
        }
        return true
    }


    fun reset(sender: CommandSender, displayName: String): Boolean {
        val display = getDisplay(displayName) ?: return false

        // reset to default
        display.dithering = false
        display.fastDithering = false
        display.showStatus = false
        display.monochrome = false
        display.sepia = false
        display.dithering = false
        display.fastDithering = false
        display.showStatus = false
        display.flip = false
        display.invert = false
        display.saturationLevel = 1.0
        display.colorEnhancer = false
        display.keepAspectRatio = false
        display.aspectRatioWidth = 16.0
        display.aspectRatioHeight = 9.0
        display.noise = false
        display.noiseLevel = DEFAULT_NOISE_LEVEL
        display.sobel = false
        display.sobelLevel = DEFAULT_SOBEL_LEVEL
        display.quantize = false
        display.quantizeLevel = DEFAULT_QUANTIZE_LEVEL
        display.cartoon = false
        display.blur = false
        display.blurRadius = DEFAULT_BLUR_RADIUS
        display.denoise = false
        display.denoiseRadius = DEFAULT_DENOISE_RADIUS
        display.contrast = false
        display.contrastLevel = DEFAULT_CONTRAST_LEVEL
        display.scanline = false
        display.scanlineHeight = DEFAULT_SCANLINE_HEIGHT
        display.sharpen = false
        display.sharpenLevel = DEFAULT_SHARPEN_LEVEL
        display.brightness = false
        display.brightnessLevel = DEFAULT_BRIGHTNESS_LEVEL
        display.distance = DEFAULT_DISTANCE
        display.parallelDithering = false
        display.parallelism = DEFAULT_PARALLELISM
        display.rasterNoise = false
        display.rasterNoiseLevel = DEFAULT_RASTER_NOISE_LEVEL
        display.vignette = false
        display.vignetteLevel = DEFAULT_VIGNETTE_LEVEL

        display.testMode = false

        // update&save
        display.refreshFlag = true
        save(sender)
        return true
    }

    fun saveImage(player:Player,name:String): Boolean{
        val display = getDisplay(name) ?: return false
        val fileName = Main.imageManager.createKey(player.name)

        if(!Main.imageManager.save(fileName,display.currentImage!!)){
           player.sendMessage(Main.prefix + "§c§l Failed to save image")
           return false
        }

        return true
    }
}