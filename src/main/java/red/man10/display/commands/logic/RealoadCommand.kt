package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.ImageLoader
import red.man10.display.Main

class ReloadCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        plugin.reloadConfig()
        ImageLoader.clearCache()
        Main.settings.load(plugin, plugin.config)
        Main.displayManager.deinit()
        Main.displayManager.load()
        sender.sendMessage(Main.prefix + "§a§l reloaded")
        return true
    }
}