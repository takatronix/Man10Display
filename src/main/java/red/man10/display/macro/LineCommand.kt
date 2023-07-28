package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.ImageLoader
import red.man10.display.MacroCommand
import red.man10.display.MacroCommandHandler
import red.man10.extention.drawLine

class LineCommand(private var macroName:String,private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        val x = macroCommand.params[0].toInt()
        val y = macroCommand.params[1].toInt()
        val x2 = macroCommand.params[2].toInt()
        val y2 = macroCommand.params[3].toInt()
        display.update(display.currentImage?.drawLine(x,y,x2,y2))
    }
}