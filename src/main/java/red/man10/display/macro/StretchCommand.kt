package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.ImageLoader
import red.man10.display.MacroCommand
import red.man10.display.MacroCommandHandler
import red.man10.extention.stretchImage

class StretchCommand(private var macroName:String,private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        val fileName = macroCommand.params[0]
        //　　キャッシュにすでに読み込み済みならそれを送信する
        if(display.packetCache[fileName] != null){
            display.sendMapCache(players,"current")
            return
        }
        // 画像を読み込み全画面更新
        display.currentImage?.stretchImage(display.filterImage(ImageLoader.get(fileName)!!))
        display.createPacketCache(display.currentImage!!,fileName)
        display.refresh()
    }
}