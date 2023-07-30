package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.extention.color

class ColorCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        // 一つ目の引数の先頭が#ならカラーコードとして扱う
        if (macroCommand.params[0].startsWith("#")) {
            display.currentImage?.color(macroCommand.params[0])
            return
        }
        val r = macroCommand.params[0].toInt()
        val g = macroCommand.params[1].toInt()
        val b = macroCommand.params[2].toInt()
        display.currentImage?.color(r, g, b)
    }
}