package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class PlaceGrowingCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            if (sender !is Player) {
                sender.sendMessage(Main.prefix + "§c§l This command can only be executed by players")
                return false
            }
            val player = sender
            val name = args[1]
            val display = Main.displayManager.getDisplay(name)
            if (display == null) {
                sender.sendMessage(Main.prefix + "§c§l $name does not exist")
                return false
            }

            sender.sendMessage(Main.prefix + "§a§l Creating a display where you are looking at")
            return Main.displayManager.setupDisplay(display, player, true)
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l${e.message}")
            return true
        }
    }
}