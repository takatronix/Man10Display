package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.extention.fill

class ClearCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {

        var sendFlag = true
        if (macroCommand.params.size >= 1) {
            val filters = macroCommand.params[0].split(",")
            for (filter in filters) {
                if (filter == "noupdate") {
                    sendFlag = false
                }
            }

        }
        val rect = display.currentImage?.fill("#000000")
        if (sendFlag) {
            display.update(rect)
        }
    }
}