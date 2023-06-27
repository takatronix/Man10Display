package red.man10.display

import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class KitManager {
    companion object{

        //      キットを保存する
        fun save(p: CommandSender, kitName: String): Boolean {
            if(p !is Player){
                p.sendMessage("プレイヤーのみ実行できます")
                return false
            }
            val userdata = File(Main.plugin!!.dataFolder, File.separator + "kits")
            val f = File(userdata, File.separator + kitName + ".yml")
            val data: FileConfiguration = YamlConfiguration.loadConfiguration(f)
            if (!f.exists()) {
                try {
                    data["creator"] = p.name
                    data["inventory"] = p.inventory.contents
                    data["armor"] = p.inventory.armorContents
                    data.save(f)
                    info( "キットを保存しました:$kitName",p)
                } catch (exception: Exception) {
                    error("キットの保存に失敗した" + exception.message,p)
                }
            }
            return true
        }

        //      キットを読み込む
        fun load(p: CommandSender, kitName: String): Boolean {
            if(p !is Player){
                p.sendMessage("プレイヤーのみ実行できます")
                return false
            }
            val userdata = File(Main.plugin!!.dataFolder, File.separator + "kits")
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
        fun delete(p: CommandSender, kitName: String): Boolean {val userdata = File(Main.plugin!!.dataFolder, File.separator + "kits")
            val f = File(userdata, File.separator + kitName + ".yml")
            if (!f.exists()) {
                p.sendMessage("キットは存在しない:$kitName")
                return false
            }
            f.delete()
            info( "${kitName}キットを削除しました",p)
            return true
        }

        fun getList(): List<String> {
            val folder = File(Main.plugin!!.dataFolder, File.separator + "kits")
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

        //      キット一覧
        fun showList(p: CommandSender): Boolean {
            p.sendMessage("§e§l========== 登録済みのキット =========")
            getList().forEachIndexed { index, s ->
                p.sendMessage("§e§l${index+1}: §f§l$s")
            }
            p.sendMessage("§e§l===================================")
            return true
        }
    }

}