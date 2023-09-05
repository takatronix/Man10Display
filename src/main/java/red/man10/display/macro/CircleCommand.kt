package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.extention.drawCircle
import red.man10.extention.drawFillCircle
import red.man10.extention.toColor
import java.awt.Color

class CircleCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {

        var x = 0.0
        var y = 0.0
        var r = 0.0

        var color = Color.white
        val params = macroCommand.params[0].split(",")
        var sendFlag = true
        var fill = false
        for (param in params) {
            if (param == "noupdate") {
                sendFlag = false
            }
            if (param == "update") {
                sendFlag = true
            }
            if (param == "fill") {
                fill = true
            }
            if (param.startsWith("x=")) {
                x = param.replace("x=", "").toDouble()
            }
            if (param.startsWith("y=")) {
                y = param.replace("y=", "").toDouble()
            }
            if (param.startsWith("r=")) {
                r = param.replace("r=", "").toDouble()
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

        val rect = if (fill) display.currentImage?.drawFillCircle(x.toInt(), y.toInt(), r.toInt(), color) else
            display.currentImage?.drawCircle(x.toInt(), y.toInt(), r.toInt(), color)

        if (sendFlag) {
            display.update(rect)
        }
    }
}