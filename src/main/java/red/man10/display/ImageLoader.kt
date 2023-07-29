package red.man10.display

import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

class ImageLoader(fileName: String) {

    companion object{
        private var imageCache: MutableMap<String, BufferedImage> = ConcurrentHashMap()
        fun load(filePath: String): BufferedImage? {
            return try {
                // httpから始まる場合は、URLから画像を取得
                if(filePath.startsWith("http")){
                    return ImageIO.read(java.net.URL(filePath))
                }

                // ファイルが存在しない場合、プラグインフォルダを検索
                if(!File(filePath).exists()){
                    val pluginFile = "${Main.plugin.dataFolder}/$filePath"
                    if(File(pluginFile).exists()){
                        return ImageIO.read(File(pluginFile).absoluteFile)
                    }
                }

                ImageIO.read(File(filePath))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        fun get(name:String):BufferedImage?{
            if(imageCache.containsKey(name)){
                return imageCache[name]
            }
            info("loading image:$name")
            val image = load(name) ?: return null
            imageCache[name] = image
            return image
        }
        fun clearCache(){
            imageCache.clear()
        }
    }
}