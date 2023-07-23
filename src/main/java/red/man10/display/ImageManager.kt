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

fun BufferedImage.drawRectangle(x:Int,y:Int,width:Int,height:Int,color:Int){
    this.graphics.color = java.awt.Color(color)
    this.graphics.drawRect(x,y,width,height)
}
fun BufferedImage.drawImage(image: BufferedImage) {
    // 描画先の画像サイズ
    val targetWidth = this.width
    val targetHeight = this.height

    // 元画像のサイズ
    val sourceWidth = image.width
    val sourceHeight = image.height

    // アスペクト比を維持しながら全体が収まるように調整
    val aspect = sourceWidth.toDouble() / sourceHeight.toDouble()
    val newWidth: Int
    val newHeight: Int
    if (targetWidth / aspect <= targetHeight) {
        // 幅に合わせる場合
        newWidth = targetWidth
        newHeight = (newWidth / aspect).toInt()
    } else {
        // 高さに合わせる場合
        newHeight = targetHeight
        newWidth = (newHeight * aspect).toInt()
    }

    // 最大値を超えないように調整
    val maxWidth = targetWidth.coerceAtMost(newWidth)
    val maxHeight = targetHeight.coerceAtMost(newHeight)

    // 中央に描画
    val x = (targetWidth - maxWidth) / 2
    val y = (targetHeight - maxHeight) / 2

    this.graphics.drawImage(image, x, y, maxWidth, maxHeight, null)
}

fun BufferedImage.stretchImage(image:BufferedImage){
    this.graphics.drawImage(image,0,0,this.width,this.height,null)
}

fun BufferedImage.clear(color: Color = Color.BLACK){
    val g: Graphics2D = this.createGraphics()
    g.color = color
    g.fillRect(0, 0, this.width, this.height)
    g.dispose()
}


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