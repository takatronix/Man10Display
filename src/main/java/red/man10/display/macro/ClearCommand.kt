package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.MacroCommand
import red.man10.display.MacroCommandHandler
import red.man10.extention.color
import red.man10.extention.fill

class ClearCommand(private var macroName:String,private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override  fun run(display: Display, players:List<Player>, sender: CommandSender? ){
        display.currentImage?.color(0,0,0)
        display.update(display.currentImage?.fill())
    }
}