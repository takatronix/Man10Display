package red.man10.display

import ImageDisplay
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
import java.io.File


class DisplayManager(main: JavaPlugin)   : Listener {
    val displays = mutableListOf<Display<Any?>>()
    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)
    }

    fun deinit(){
        for (display in displays) {
            if(display is StreamDisplay){
                display.deinit()
            }
        }
        displays.clear()
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
            return arrayListOf("fps","interval","refresh","dithering","fast_dithering","show_status","monochrome","flip","keep_aspect_ratio","aspect_ratio_width","aspect_ratio_height","test_mode")
        }
    fun getDisplay(name: String): Display<Any?>? {
        displays.find { it.name == name }?.let {
            return it
        }
        return null
    }

    fun create(player: Player,display: Display<Any?>) : Boolean{
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
            p.sendMessage("§a§l ${display.name}")
        }
        return true
    }
    fun showInfo(p: CommandSender, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        p.sendMessage(Main.prefix + "§a§l Display Info")
        p.sendMessage("§a§l name: ${display.name}")
        p.sendMessage("§a§l width: ${display.width}")
        p.sendMessage("§a§l height: ${display.height}")
        p.sendMessage("§a§l location: ${display.location}")
        p.sendMessage("§a§l fps: ${display.fps}")

        // パラメータを表示
        p.sendMessage("§a§l monochrome: ${display.monochrome}")
        p.sendMessage("§a§l dithering: ${display.dithering}")
        p.sendMessage("§a§l fast_dithering: ${display.fastDithering}")
        p.sendMessage("§a§l show_status: ${display.showStatus}")
        p.sendMessage("§a§l flip: ${display.flip}")
        p.sendMessage("§a§l keep_aspect_ratio: ${display.keepAspectRatio}")
        p.sendMessage("§a§l aspect_ratio_width: ${display.aspectRatioWidth}")
        p.sendMessage("§a§l aspect_ratio_height: ${display.aspectRatioHeight}")
        p.sendMessage("§a§l test_mode: ${display.testMode}")

        return true
    }

    private fun createMaps(display:Display<Any?>, player: Player, xSize:Int, ySize:Int): Boolean {
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

    private fun getMaps(display:Display<Any?>, player: Player): Boolean {
        val items = getMaps(display)
        for(item in items){
            player.world.dropItem(player.location,item)
        }
        return true
    }

    fun getMaps(display: Display<Any?>): ArrayList<ItemStack> {
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
        }
        return true
    }

    fun set(sender: CommandSender, displayName: String, key: String, value: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        val ret = display.set(sender,key,value)
        if(ret)
            save(sender)
        return ret
    }
}