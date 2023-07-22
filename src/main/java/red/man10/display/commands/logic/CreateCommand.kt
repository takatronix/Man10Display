package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.ImageDisplay
import red.man10.display.Main
import red.man10.display.StreamDisplay

class CreateCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try{
            val type = args[1]
            val name = args[2]
            val width = args[3].toInt()
            val height = args[4].toInt()

            if(type == "stream"){
                val port = args[5].toInt()
                val newDisplay = StreamDisplay(name, width, height, port)
                if(!Main.displayManager.create(sender as Player, newDisplay)){
                    sender.sendMessage(Main.prefix + "§a§l $name already exists")
                    return false
                }
            }
            if(type == "image"){
                if(!Main.displayManager.create(sender as Player, ImageDisplay(name, width, height))){
                    sender.sendMessage(Main.prefix + "§a§l $name already exists")
                    return false
                }
            }

            Main.displayManager.save(sender)
            sender.sendMessage(Main.prefix + "§a§l $name created")
        }catch (e:Exception){
            sender.sendMessage(Main.prefix + "§c§l ${e.message}")
            return true
        }
        return true
    }
}