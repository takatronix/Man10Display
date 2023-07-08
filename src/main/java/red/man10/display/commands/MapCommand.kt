package red.man10.display.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class MapCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try{
            val name = args[1]
            if(!Main.displayManager.map(sender,name)){
                sender.sendMessage(Main.prefix + "§a§l $name does not exist")
                return false
            }
        }catch (e:Exception){
            sender.sendMessage(Main.prefix + "§c§l{e.message}")
            return true
        }
        return true
    }
}