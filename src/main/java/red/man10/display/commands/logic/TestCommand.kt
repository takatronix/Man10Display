package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main
import red.man10.extention.get


class TestCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            if (sender !is Player) {
                sender.sendMessage(Main.prefix + "§c§lThis command can only be executed by players.")
                return true
            }
            // 手に持ってるアイテムのPersistentDataを取得
            val itemInHand = sender.inventory.itemInMainHand
            val data = itemInHand.itemMeta.persistentDataContainer
            // PersistentDataの中身を取得
            val type = data.get(Main.plugin, "man10display.type", PersistentDataType.STRING)
            val width = data.get(Main.plugin, "man10display.pen.width", PersistentDataType.INTEGER)
            val color = data.get(Main.plugin, "man10display.pen.color", PersistentDataType.STRING)

            // PersistentDataの中身を表示
            sender.sendMessage(Main.prefix + "§a§lType: $type")
            sender.sendMessage(Main.prefix + "§a§lWidth: $width")
            sender.sendMessage(Main.prefix + "§a§lColor: $color")


        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l{$e.message}")
        }
        return true
    }
}