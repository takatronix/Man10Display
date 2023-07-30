package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class StopCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return try {
            val displayName = args[1]
            // マクロを停止し、ブランクマップを送信する
            Main.displayManager.runMacro(sender, displayName, null)
            val display = Main.displayManager.getDisplay(displayName)
            display?.reset()
            true
        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l{$e.message}")
            true
        }
    }
}