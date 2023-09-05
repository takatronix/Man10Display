package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.info
import red.man10.extention.drawPolygon
import red.man10.extention.fillPolygon
import red.man10.extention.toColor

class PolygonCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {


        var color = java.awt.Color.white
        val polygons = macroCommand.params[0].split(",")
        val xPoints = mutableListOf<Int>()
        val yPoints = mutableListOf<Int>()
        for (polygon in polygons) {
            val points = polygon.split(":")
            xPoints.add(points[0].toInt())
            yPoints.add(points[1].toInt())
            info("x:${points[0].toInt()},y:${points[1].toInt()}")
        }

        val params = macroCommand.params[1].split(",")
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

        val rect =
            if (fillFlag)
                display.currentImage?.fillPolygon(xPoints.toIntArray(), yPoints.toIntArray(), color)
            else
                display.currentImage?.drawPolygon(xPoints.toIntArray(), yPoints.toIntArray(), color)

        if (sendFlag) {
            display.update(rect)
        }
    }
}