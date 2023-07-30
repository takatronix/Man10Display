package red.man10.display.commands.logic

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class WandCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§2§l Only players can execute this command")
            return true
        }
        val item = ItemStack(Material.STICK)
        item.editMeta {
            it.displayName(Component.text("${Main.prefix}§dItem frame wand"))
            it.persistentDataContainer.set(
                NamespacedKey(Main.plugin, "Man10DisplayWand"),
                PersistentDataType.INTEGER,
                1
            )
        }

        sender.inventory.setItemInMainHand(item)

        return true
    }
}