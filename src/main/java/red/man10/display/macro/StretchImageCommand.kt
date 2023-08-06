package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.ImageLoader
import red.man10.extention.clear
import red.man10.extention.drawImage
import red.man10.extention.drawTextCenter
import java.awt.Color

class StretchImageCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        val fileName = macroCommand.params[0].replace("\"", "")

        //　　キャッシュにすでに読み込み済みならそれを送信する
        if (display.packetCache[fileName] != null) {
            display.sendMapCache(players, fileName)
            return
        }

        val image = display.currentImage?:return
        image.clear()
        val getImage = ImageLoader.get(fileName)
        if(getImage == null){
            image.drawTextCenter("file not found $fileName",13.0f, Color.RED)
            display.createPacketCache(image, "error")
            display.sendMapCache("error")
            return
        }

        image.drawImage(getImage)
        display.createPacketCache(image, fileName)
        display.sendMapCache(fileName)
    }
}