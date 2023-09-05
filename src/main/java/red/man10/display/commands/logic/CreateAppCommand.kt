package red.man10.display.commands.logic

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main
import red.man10.extention.setPersistentData

class CreateAppCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            if (sender !is Player) {
                sender.sendMessage(Main.prefix + "§c§lThis command can only be executed by players.")
                return true
            }
            val macroName = args[1]

            val item = ItemStack(Material.FILLED_MAP)
            val mapMeta = item.itemMeta as MapMeta
            mapMeta.mapView = Bukkit.getMap(Main.appManager.getMapId(sender)!!)
            item.itemMeta = mapMeta

            // アイテム名とLore
            val meta: ItemMeta = item.itemMeta
            meta.setDisplayName("§e§l${macroName}")
            //meta.lore = listOf("§e§l${macroName}")
            item.itemMeta = meta


            item.setPersistentData(Main.plugin, "man10display.app.macro", PersistentDataType.STRING, macroName)
            item.setPersistentData(Main.plugin, "man10display.app.key", PersistentDataType.STRING, "macro")
            sender.world.dropItem(sender.location, item)
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l ${e.message}")
            return true
        }
        return true
    }
}
