package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.ImageLoader
import red.man10.extention.*
import java.awt.Color
import java.awt.image.BufferedImage

class ImageCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        val fileName = macroCommand.params[0].replace("\"", "")

        // もしx,y,x2,y2が指定されていたらそこに出力
        if (macroCommand.params.size == 5) {
            val x = macroCommand.params[1].toInt()
            val y = macroCommand.params[2].toInt()
            val x2 = macroCommand.params[3].toInt()
            val y2 = macroCommand.params[4].toInt()
            //   display.update(display.currentImage?.drawImage(ImageLoader.get(fileName)!!,x,y,x2,y2))
            return
        }

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