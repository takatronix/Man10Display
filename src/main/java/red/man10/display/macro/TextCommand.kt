package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.info
import red.man10.extention.*
import java.awt.Color
import java.awt.Rectangle

class TextCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {

        var text = macroCommand.params[0].replace("\"", "")
        info("text $text")
        var sendFlag = true
        var key = text
        var filterParams = mutableListOf<String>()
        var useCache = true
        var stretch = false
        var transparent = false
        var useXY = false

        var textColor = Color.WHITE
        var x = 0.0
        var y = 0.0
        var pos = "center"
        var fontSize = 13.0f
        if (macroCommand.params.size >= 2) {
            val filterParam = macroCommand.params[1].replace("\"", "")
            filterParams = filterParam.split(",").toMutableList()
            for (param in filterParams) {
                info("filter param $param")
                if (param == "nocache") {
                    useCache = false
                }
                if (param == "noupdate") {
                    sendFlag = false
                }
                if (param == "update") {
                    sendFlag = true
                }
                if (param.startsWith("x=")) {
                    x = param.replace("x=", "").toDouble()
                    useXY = true
                }
                if (param.startsWith("y=")) {
                    y = param.replace("y=", "").toDouble()
                    useXY = true
                }
                if (param.startsWith("pos=")) {
                    pos = param.replace("pos=", "")
                }
                if (param.startsWith("color=")) {
                    textColor = param.replace("color=", "").toColor()
                }
                if (param.startsWith("c=")) {
                    textColor = param.replace("c=", "").toColor()
                }
                if (param.startsWith("size=")) {
                    fontSize = param.replace("size=", "").toFloat()
                }
            }
        }

        val image = display.currentImage ?: return
        if (useXY) {
            val rect = image.drawText(x.toInt(), y.toInt(), text, fontSize, textColor)
            if (sendFlag) {
                display.update(rect)
            }
            return
        }

        var rect: Rectangle? = null
        when (pos) {
            "center" -> rect = image.drawTextCenter(text, fontSize, textColor)
            "topCenter" -> rect = image.drawTextTop(text, fontSize, textColor)
            "top" -> rect = image.drawTextTop(text, fontSize, textColor)
            "topRight" -> rect = image.drawTextTopRight(text, fontSize, textColor)
            "topLeft" -> rect = image.drawTextTopLeft(text, fontSize, textColor)
            "bottom" -> rect = image.drawTextBottom(text, fontSize, textColor)
            "bottomLeft" -> rect = image.drawTextBottomLeft(text, fontSize, textColor)
            "bottomRight" -> rect = image.drawTextBottomRight(text, fontSize, textColor)
            "left" -> rect = image.drawTextLeft(text, fontSize, textColor)
            "right" -> rect = image.drawTextRight(text, fontSize, textColor)
        }
        if (sendFlag) {
            display.update(rect)
        }
    }

}