package red.man10.display

import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

class ImageLoader {
    companion object{
        private var imageCache: MutableMap<String, BufferedImage> = ConcurrentHashMap()
        fun loadImage(filePath: String): BufferedImage? {
            return try {
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
            val image = loadImage(name) ?: return null
            imageCache[name] = image
            return image
        }
        fun clearCache(){
            imageCache.clear()
        }
    }
}