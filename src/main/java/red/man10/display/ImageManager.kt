package red.man10.display

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

class ImageManager(imagePath:String) {
    private var imagePath: String? = imagePath

    companion object{
    }

    fun save(name:String,image:BufferedImage):Boolean{
        try{
            // ディレクトリを作成
            val dir = File(imagePath!!)
            if(!dir.exists()){
                dir.mkdirs()
            }
            val file = File(imagePath,name)
            info("saving image:${file.absoluteFile}")
            ImageIO.write(image,"png",file)
            info("saved image:${file.absoluteFile}")
        }catch (e:Exception){
            e.printStackTrace()
            return false
        }
        return true
    }
    fun createKey(name: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val formatted = LocalDateTime.now().format(formatter)
        return "$name-$formatted.png"
    }
}