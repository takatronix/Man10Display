package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.extention.fill
import red.man10.extention.toColor

class FillCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {

        val color = macroCommand.params[0]
        var sendFlag = true
        if (macroCommand.params.size >= 2) {
            val filters = macroCommand.params[1].split(",")
            for (filter in filters) {
                if (filter == "noupdate") {
                    sendFlag = false
                }
                if (filter == "update") {
                    sendFlag = true
                }
            }

        }

        val rect = display.currentImage?.fill(color.toColor())
        if (sendFlag) {
            display.update(rect)
        }
    }
}