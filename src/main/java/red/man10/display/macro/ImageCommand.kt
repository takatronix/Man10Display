package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display
import red.man10.display.ImageLoader
import red.man10.display.filter.ParameterFilter
import red.man10.display.info
import red.man10.extention.*
import java.awt.Color

class ImageCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        val fileName = macroCommand.params[0].replace("\"", "")

        var x = 0.0
        var y = 0.0
        var h = 0.0
        var w = 0.0
        var useCache = true
        var stretch = false
        var transparent = false
        var changePos = false
        var sendFlag = true
        var filterParams = mutableListOf<String>()
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
                if (param == "stretch") {
                    stretch = true
                }
                if (param == "transparent") {
                    transparent = true
                }
                if (param == "noclear") {
                    transparent = true
                }
                if (param.startsWith("x=")) {
                    x = param.replace("x=", "").toDouble()
                    changePos = true
                }
                if (param.startsWith("y=")) {
                    y = param.replace("y=", "").toDouble()
                    changePos = true
                }
                if (param.startsWith("h=")) {
                    h = param.replace("h=", "").toDouble()
                    changePos = true
                }
                if (param.startsWith("w=")) {
                    w = param.replace("w=", "").toDouble()
                    changePos = true
                }
            }
        }
        if (changePos) {
            transparent = true

        }

        //　　キャッシュにすでに読み込み済みならそれを送信する
        if (useCache && display.packetCache[fileName] != null) {
            display.sendMapCache(players, fileName)
            return
        }
        var image = display.currentImage ?: return
        if (!transparent) {
            image.clear()
        }

        val getImage = ImageLoader.get(fileName, useCache)
        if (getImage == null) {
            image.drawTextCenter("file not found $fileName", 13.0f, Color.RED)
            display.createPacketCache(image, "error")
            display.sendMapCache("error")
            return
        }

        if (stretch) {
            if (changePos) {
                image.drawImageStretch(getImage, x.toInt(), y.toInt(), w.toInt(), h.toInt())
                info("stretch x:$x y:$y w:$w h:$h")
            } else {
                image.drawImageStretch(getImage)
            }
        } else {
            if (changePos) {
                image.drawImage(getImage, x.toInt(), y.toInt(), w.toInt(), h.toInt())
                info("draw x:$x y:$y w:$w h:$h")
            } else {
                image.drawImageCenter(getImage)
            }
        }

        // フィルタ設定があれば出来上がった画像を送信
        for (param in filterParams) {
            image = ParameterFilter(param).apply(image)
        }

        display.createPacketCache(image, fileName, sendFlag)
    }
}