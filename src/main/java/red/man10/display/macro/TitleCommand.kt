package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.ImageLoader
import red.man10.display.filter.ParameterFilter
import red.man10.extention.clear
import red.man10.extention.drawImage
import red.man10.extention.drawTextCenter
import red.man10.extention.stretchImage
import java.awt.Color

class TitleCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        val text = macroCommand.params[0].replace("\"", "")

        var useCache = true
        var stretch = false
        var filterParams = mutableListOf<String>()
        if (macroCommand.params.size >= 2) {
            val filterParam = macroCommand.params[1].replace("\"", "")
            filterParams = filterParam.split(",").toMutableList()
        }

        var image = display.currentImage ?: return



/*
        //　　キャッシュにすでに読み込み済みならそれを送信する
        if (useCache && display.packetCache[fileName] != null) {
            display.sendMapCache(players, fileName)
            return
        }
        var image = display.currentImage ?: return
        image.clear()

        val getImage = ImageLoader.get(fileName, useCache)
        if (getImage == null) {
            image.drawTextCenter("file not found $fileName", 13.0f, Color.RED)
            display.createPacketCache(image, "error")
            display.sendMapCache("error")
            return
        }

        if (stretch) {
            image.stretchImage(getImage)
        } else {
            image.drawImage(getImage)
        }

        for (param in filterParams) {
            image = ParameterFilter(param).apply(image)
        }

        display.createPacketCache(image, fileName)
        display.sendMapCache(fileName)
        */

    }


}