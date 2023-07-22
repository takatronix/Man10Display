package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class MacroCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try{
            val displayName = args[1]
            val mode = args[2]
            val macroName = args[3]
            if(mode =="run"){
                Main.displayManager.runMacro(sender,displayName,macroName)
                return true
            }
            if(mode == "list"){
                Main.displayManager.showMacroList(sender)
                return true
            }
            // マクロ停止
            Main.displayManager.runMacro(sender,displayName,null)
            return true
        }catch (e:Exception){
            sender.sendMessage(Main.prefix + "§c§l{e.message}")
            return true
        }
        return true
    }
}