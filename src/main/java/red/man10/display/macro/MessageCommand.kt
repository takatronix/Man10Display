package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.MacroCommand
import red.man10.display.MacroCommandHandler

class MessageCommand(private var macroName:String,private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override  fun run(display: Display, players:List<Player>, sender: CommandSender? ){
        val mes = macroCommand.params[0]
        for( player in players){
            player.sendMessage(mes)
        }
    }
}