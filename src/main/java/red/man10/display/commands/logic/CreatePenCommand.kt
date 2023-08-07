package red.man10.display.commands.logic

import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main
import red.man10.extention.setPersistentData
import red.man10.extention.toColor
import red.man10.extention.toHexRGB

class CreatePenCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            if (sender !is Player) {
                sender.sendMessage(Main.prefix + "§c§lThis command can only be executed by players.")
                return true
            }
            var player = sender
            val width = args[1].toInt()
            val color = args[2].toColor()

            val itemInHand = player.inventory.itemInMainHand


            // もし手にアイテムがないならエラーメッセージ
            if (player.inventory.itemInMainHand.type.isAir) {
                player.sendMessage(Main.prefix + "§c§lYou must hold an item in your hand.")
                return true
            }
            val meta: ItemMeta = itemInHand.itemMeta
            meta.setDisplayName("§f§lDisplay Pen / Width:$width / Color:#${color.toHexRGB()}")
            itemInHand.itemMeta = meta

            itemInHand.setPersistentData(Main.plugin, "man10display.type", PersistentDataType.STRING, "pen")
            itemInHand.setPersistentData(Main.plugin, "man10display.pen.width", PersistentDataType.INTEGER, width)
            itemInHand.setPersistentData(
                Main.plugin,
                "man10display.pen.color",
                PersistentDataType.STRING,
                color.toHexRGB()
            )

            player.sendMessage(Main.prefix + "§a§lYou have successfully created a pen.")

        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l ${e.message}")
            return true
        }
        return true
    }

    fun setItemStackPersistentData(plugin: Plugin, item: ItemStack, key: String, value: String) {
        val itemMeta = item.itemMeta   // Get the item's metadata

        val namespacedKey = NamespacedKey(plugin, key)  // Create a namespaced key for the data

        itemMeta.persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, value)  // Set the data

        item.itemMeta = itemMeta  // Apply the new metadata to the item
    }
}