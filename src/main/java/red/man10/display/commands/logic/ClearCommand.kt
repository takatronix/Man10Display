package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main
import red.man10.extention.fill


class ClearCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return try {
            val displayName = args[1]
            val display = Main.displayManager.getDisplay(displayName)
            display?.currentImage?.fill()
            display?.update()
            true
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l{$e.message}")
            true
        }
    }
}