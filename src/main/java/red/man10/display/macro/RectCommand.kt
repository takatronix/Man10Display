package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.extention.drawRect
import red.man10.extention.fillRect
import red.man10.extention.toColor
import java.awt.Color

class RectCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {

        var x = 0.0
        var y = 0.0
        var w = 0.0
        var h = 0.0

        var color = Color.white
        val params = macroCommand.params[0].split(",")
        var sendFlag = true
        var fillFlag = false
        for (param in params) {
            if (param == "noupdate") {
                sendFlag = false
            }
            if (param == "update") {
                sendFlag = true
            }
            if (param == "fill") {
                fillFlag = true
            }
            if (param.startsWith("x=")) {
                x = param.replace("x=", "").toDouble()
            }
            if (param.startsWith("y=")) {
                y = param.replace("y=", "").toDouble()
            }
            if (param.startsWith("h=")) {
                h = param.replace("h=", "").toDouble()
            }
            if (param.startsWith("w=")) {
                w = param.replace("w=", "").toDouble()
            }
            // color
            if (param.startsWith("color=")) {
                val colorStr = param.replace("color=", "")
                color = colorStr.toColor()
            }
            if (param.startsWith("c=")) {
                val colorStr = param.replace("c=", "")
                color = colorStr.toColor()
            }

        }

        val rect = if (fillFlag) display.currentImage?.fillRect(x.toInt(), y.toInt(), w.toInt(), h.toInt(), color) else

            display.currentImage?.drawRect(x.toInt(), y.toInt(), w.toInt(), h.toInt(), color)
        if (sendFlag) {
            display.update(rect)
        }
    }
}