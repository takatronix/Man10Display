package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class RunCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return try {
            val displayName = args[1]
            val macroName = args[2]
            return Main.displayManager.runMacro(sender, displayName, macroName)
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l Macro error:{$e.message}")
            true
        }
    }
}