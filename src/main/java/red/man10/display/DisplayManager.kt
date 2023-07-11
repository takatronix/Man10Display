package red.man10.display

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class DisplayManager(main: JavaPlugin)   : Listener {
    private val displayFolder = "displays"
    private val displays = mutableListOf<Display>()
    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.plugin, Runnable {
            sendMapPacketsTask()
        }, 0, 1)
    }

    public fun deinit(){
        for (display in displays) {
            display.deinit()
        }
        displays.clear()
    }


    private fun sendMapPacketsTask(){
        for (display in displays) {


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
    fun findDisplay(mapId: Int): Display? {
        for (display in displays) {
            if(display.mapIds.contains(mapId)){
                return display
            }
        }
        return null
    }

    private fun getDisplay(name: String): Display? {

        for (display in displays) {
            if (display.name == name) {
                return display
            }
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
        displays.remove(display)
        return true
    }
    fun map(p: CommandSender, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        return true
    }

    private fun createMaps(display:Display, player: Player, xSize:Int, ySize:Int): Boolean {
        for(y in 0 until ySize){
            for(x in 0 until xSize){
                val mapView = Bukkit.getServer().createMap(player.world)
                mapView.scale = MapView.Scale.CLOSEST
                mapView.isUnlimitedTracking = true

                //for (renderer in mapView.renderers) {
                //    mapView.removeRenderer(renderer)
                // }

                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = mapView

                val name = "${display.name}_${x}_${y}_${mapView.id}"
                mapMeta.displayName(Component.text(name))
                itemStack.itemMeta = mapMeta

                player.world.dropItem(player.location,itemStack)
                display.mapIds.add(mapView.id)
                player.sendMessage("$name created")
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

    //      キットを読み込む
    fun load(p: CommandSender, kitName: String): Boolean {
        if(p !is Player){
            p.sendMessage("プレイヤーのみ実行できます")
            return false
        }
        val userdata = File(Main.plugin!!.dataFolder, File.separator + displayFolder)
        val f = File(userdata, File.separator + kitName + ".yml")
        val data: FileConfiguration = YamlConfiguration.loadConfiguration(f)
        if (!f.exists()) {
            p.sendMessage("キットは存在しない:$kitName")
            return false
        }
        val inventoryList =  data["inventory"] as List<ItemStack>
        val armorList =  data["armor"] as List<ItemStack>

        p.inventory.clear()
        inventoryList.forEachIndexed { index, itemStack ->
            p.inventory.setItem(index,itemStack)
        }
        p.inventory.armorContents = armorList.toTypedArray()

        return true
    }

    fun getList(): List<String> {
        val folder = File(Main.plugin!!.dataFolder, File.separator + displayFolder)
        val files = folder.listFiles()
        val list = mutableListOf<String>()
        for (f in files) {
            if (f.isFile) {
                var filename = f.name
                //      隠しファイルは無視
                if (filename.substring(0, 1).equals(".", ignoreCase = true)) {
                    continue
                }
                val point = filename.lastIndexOf(".")
                if (point != -1) {
                    filename = filename.substring(0, point)
                }
                list.add(filename)
            }
        }
        return list
    }

    fun showList(p: CommandSender): Boolean {
        p.sendMessage("§e§l========== 登録済みのキット =========")
        getList().forEachIndexed { index, s ->
            p.sendMessage("§e§l${index+1}: §f§l$s")
        }
        return true
    }
}