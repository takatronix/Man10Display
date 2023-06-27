package red.man10.display

import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

public class LocationManager {
    public var name = "location"
    private val locations = mutableMapOf<String, Location>()

    public fun teleport(sender: CommandSender,player:CommandSender, name: String) {
        if( player !is Player){
            sender.sendMessage("プレイヤーのみ実行できます")
            return
        }
        if (!locations.containsKey(name)) {
            sender.sendMessage("その名前の位置は存在しません:$name")
            return
        }
        player.teleport(locations[name]!!)
        sender.sendMessage("テレポートしました:$name")
    }

    public fun addLocation(sender: Player, name: String) {
        val location = sender.location
        locations[name] = location
        save()
        sender.sendMessage("位置を追加しました:$name")
    }

    public fun deleteLocation(sender:Player,name:String){
        if(!locations.containsKey(name)){
            sender.sendMessage("その名前の位置は存在しません:$name")
            return
        }
        locations.remove(name)
        save()
        sender.sendMessage("位置を削除しました:$name")
    }

    public fun save() {
        val userdata = File(Main.plugin!!.dataFolder, File.separator + "locations")
        val filePath = File(userdata, File.separator + name + ".yml")
        filePath.delete()
        filePath.createNewFile()
        val config = YamlConfiguration.loadConfiguration(filePath)
        for ((name, location) in locations) {
            config.set("$name.world", location.world.name)
            config.set("$name.x", location.x)
            config.set("$name.y", location.y)
            config.set("$name.z", location.z)
            config.set("$name.yaw", location.yaw)
            config.set("$name.pitch", location.pitch)
        }
        config.save(filePath)
    }
    public fun load(): Boolean {
        val userdata = File(Main.plugin!!.dataFolder, File.separator + "locations")
        val filePath = File(userdata, File.separator + name + ".yml")
        if (!filePath.exists())
            return false
        val config = YamlConfiguration.loadConfiguration(filePath)
        for (key in config.getKeys(false)) {
            val worldName = config.getString("$key.world")
            val x = config.getDouble("$key.x")
            val y = config.getDouble("$key.y")
            val z = config.getDouble("$key.z")
            val yaw = config.getDouble("$key.yaw").toFloat()
            val pitch = config.getDouble("$key.pitch").toFloat()
            val world = Bukkit.getWorld(worldName!!)
            locations[key] = Location(world, x, y, z, yaw, pitch)
        }
        return true
    }
    fun getList(): List<String> {
        return locations.keys.toList()
    }

    fun showList(p: CommandSender): Boolean {
        p.sendMessage("§e§l========== 登録済みのロケーション =========")
        for (key in locations.keys) {
            val loc = locations[key]
            p.sendMessage("§e$key : ${loc!!.world.name} ${loc.x} ${loc.y} ${loc.z}")
        }
        p.sendMessage("§e§l===================================")
        return true
    }
}