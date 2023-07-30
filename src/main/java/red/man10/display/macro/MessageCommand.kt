package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display

class MessageCommand(private var macroName:String,private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override  fun run(display: Display, players:List<Player>, sender: CommandSender? ){
        var mes = macroCommand.params[0]
        // "があれば、前後の"を削除
        if(mes.startsWith("\"") && mes.endsWith("\"")){
            mes = mes.substring(1,mes.length-1)
        }

        for( player in players){
            player.sendMessage(mes)
        }
    }
}