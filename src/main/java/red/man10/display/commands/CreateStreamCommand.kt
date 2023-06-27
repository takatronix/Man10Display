package red.man10.display.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class CreateStreamCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {


        var name = args[1]
        var width = args[2].toInt()
        var height = args[3].toInt()
        var port = args[4].toInt()

        return true
    }
}