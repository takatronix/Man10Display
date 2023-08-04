package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class ImageCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            val name = args[1]
            var path = args[2]
            if (!Main.displayManager.delete(sender, name)) {
                sender.sendMessage(Main.prefix + "§a§l $name does not exist")
                return false
            }
            sender.sendMessage(Main.prefix + "§a§l $name deleted")
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l${e.message}")
            return true
        }
        return true
    }
}