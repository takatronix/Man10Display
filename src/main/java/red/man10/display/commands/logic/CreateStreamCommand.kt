package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main
import red.man10.display.StreamDisplay

class CreateStreamCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try{
            val name = args[1]
            val width = args[2].toInt()
            val height = args[3].toInt()
            val port = args[4].toInt()
            if(!Main.displayManager.create(sender as Player, StreamDisplay(name, width, height, port))){
                sender.sendMessage(Main.prefix + "§a§l $name already exists")
                return false
            }
            Main.displayManager.save(sender)
            sender.sendMessage(Main.prefix + "§a§l $name created")
        }catch (e:Exception){
            sender.sendMessage(Main.prefix + "§c§l{e.message}")
            return true
        }
        return true
    }
}