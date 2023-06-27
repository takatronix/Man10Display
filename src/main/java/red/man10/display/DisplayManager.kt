package red.man10.display

import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class DisplayManager(main: JavaPlugin) {
    val displayFolder = "displays"
    private val displays = mutableListOf<Display>()

    val names:ArrayList<String>
        get() {
            val nameList = arrayListOf<String>()
            for (display in displays) {
                nameList.add(display.name)
            }
            return nameList
        }


    lateinit var plugin: JavaPlugin

    fun getDisplay(name: String): Display? {
        for (display in displays) {
            if (display.name == name) {
                return display
            }
        }
        return null
    }
    fun getNameList(): List<String> {
        val nameList = mutableListOf<String>()
        for (display in displays) {
            nameList.add(display.name)
        }
        return nameList
    }

    fun addDisplay(display: Display) {
        displays.add(display)
    }

    fun printDisplays() {
        for (display in displays) {
            when (display) {
                is ImageDisplay -> {
                  //  println("red.man10.display.ImageDisplay with width ${display.width}, height ${display.height}, format ${display.imageFormat}")
                }
                is StreamDisplay -> {
                    println("red.man10.display.StreamDisplay with stream URL ${display.streamUrl}")
                }
                else -> {
                    println("Unknown display type")
                }
            }
        }
    }
    init{
        plugin = main

    }

    fun delete(p: CommandSender, kitName: String): Boolean {val userdata = File(Main.plugin!!.dataFolder, File.separator + displayFolder)
        val f = File(userdata, File.separator + kitName + ".yml")
        if (!f.exists()) {
            p.sendMessage("キットは存在しない:$kitName")
            return false
        }
        f.delete()
        info( "${kitName}キットを削除しました",p)
        return true
    }

    fun save(p: CommandSender): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "displays.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        try{


            for(display in displays){
                display.save(config,display.name)
            }
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