package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main


class TeleportCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return try {
            val displayName = args[1]
            val display = Main.displayManager.getDisplay(displayName)
            if (display?.location == null) {
                sender.sendMessage(Main.prefix + "§c§lThis display has no location.")
                return true
            }
            if (sender is Player) {
                sender.teleport(display.location!!)
                return true
            }
            sender.sendMessage(Main.prefix + "§c§lThis command can only be executed by players.")
            true
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l{$e.message}")
            true
        }
    }
}