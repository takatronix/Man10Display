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
import java.io.File


class DisplayManager(main: JavaPlugin)   : Listener {
    val displays = mutableListOf<Display>()
    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.plugin, Runnable {
            sendMapPacketsTask()
        }, 0, 2)
    }

    public fun deinit(){
        for (display in displays) {
            display.deinit()
        }
        displays.clear()
    }

    private fun sendMapPacketsTask(){
        for (display in displays) {
            if(display.modified){
                display.modified = false
                display.sendMapPacketsToPlayers2()
            }
        }
    }

    val names:ArrayList<String>
        get() {
            val nameList = arrayListOf<String>()
            for (display in displays) {
                nameList.add(display.name)
            }
            return nameList
        }
    private fun getDisplay(name: String): Display? {
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
            p.sendMessage("§a§l ${display.name}")
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
        for(y in 0 until display.height){
            for(x in 0 until display.width){
                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = Bukkit.getMap(display.mapIds[y * display.width + x])

                val name = "${x+1}-${y+1}"
                mapMeta.displayName(Component.text(name))
                itemStack.itemMeta = mapMeta
                player.world.dropItem(player.location,itemStack)
            }
        }
        return true
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
}