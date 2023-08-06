package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.ImageLoader
import red.man10.display.filter.ParameterFilter
import red.man10.extention.*
import java.awt.Color
import java.awt.image.BufferedImage

class ImageCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        val fileName = macroCommand.params[0].replace("\"", "")

        var filterParams = mutableListOf<String>()
        if (macroCommand.params.size >= 2) {
            val filterParam = macroCommand.params[1].replace("\"", "")
            filterParams = filterParam.split(",").toMutableList()
        }

        //　　キャッシュにすでに読み込み済みならそれを送信する
        if (display.packetCache[fileName] != null) {
            display.sendMapCache(players, fileName)
            return
        }
        var image = display.currentImage?:return
        image.clear()
        val getImage = ImageLoader.get(fileName)
        if(getImage == null){
            image.drawTextCenter("file not found $fileName",13.0f, Color.RED)
            display.createPacketCache(image, "error")
            display.sendMapCache("error")
            return
        }

        image.drawImage(getImage)

        for(param in filterParams){
            image = ParameterFilter(param).apply(image)
        }

        display.createPacketCache(image, fileName)
        display.sendMapCache(fileName)
    }



}