package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class SetCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            val displayName = args[1]
            val key = args[2]
            val value = args[3]

            if (!Main.displayManager.set(sender, displayName, key, value)) {
                sender.sendMessage(Main.prefix + "§a§l $displayName does not exist")
                return false
            }
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l${e.message}")
            return true
        }
        return true
    }
}