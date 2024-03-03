package red.man10.extention

import java.awt.*
import java.awt.image.BufferedImage
import kotlin.math.absoluteValue
import kotlin.math.min

fun BufferedImage.fill(colorName: String): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color(colorName)
    graphics.fillRect(0, 0, this.width, this.height)
    graphics.dispose()
    return Rectangle(0, 0, this.width, this.height)
}

fun BufferedImage.setPixel(x: Int, y: Int, color: Color): Rectangle {
    this.setRGB(x, y, color.rgb)
    return Rectangle(x, y, 1, 1)
}

fun BufferedImage.fillCircle(x: Int, y: Int, r: Int, color: Color): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.fillOval(x - r, y - r, r * 2, r * 2)
    graphics.dispose()
    return Rectangle(x - r, y - r, r * 2, r * 2)
}

fun BufferedImage.clear(): Rectangle {
    return this.fill(Color.BLACK)
}

fun BufferedImage.fill(color: Color = Color.BLACK): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.fillRect(0, 0, this.width, this.height)
    graphics.dispose()
    return Rectangle(0, 0, this.width, this.height)
}

fun BufferedImage.fill(): Rectangle {
    // 画像を塗りつぶす
    // val graphics = this.createGraphics()
    graphics.fillRect(0, 0, this.width, this.height)
    graphics.dispose()
    return Rectangle(0, 0, this.width, this.height)
}

fun BufferedImage.color(r: Int, g: Int, b: Int): Rectangle {
    val color = Color(r, g, b)
    val graphics = this.createGraphics()
    graphics.color = color
    return Rectangle(0, 0, 0, 0)
}

fun BufferedImage.color(colorCode: String): Color {
    // #を除去
    val hex = colorCode.trimStart('#')
    // r,g,bに分割
    val r = hex.substring(0, 2).toInt(16)
    val g = hex.substring(2, 4).toInt(16)
    val b = hex.substring(4, 6).toInt(16)
    val color = Color(r, g, b)
    return color
}

fun BufferedImage.drawCircle(x: Int, y: Int, radius: Int, r: Int, g: Int, b: Int): Rectangle {
    // 色を生成
    val color = Color(r, g, b)

    // 円を描画
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2)
    graphics.dispose()
    // 描画範囲をかえす
    return Rectangle(x - radius, y - radius, radius * 2, radius * 2)
}

fun BufferedImage.drawRect(x: Int, y: Int, width: Int, height: Int, color: Color): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.drawRect(x, y, width, height)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.fillRect(x: Int, y: Int, width: Int, height: Int, color: Color): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.fillRect(x, y, width, height)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}


fun BufferedImage.resize(width: Int, height: Int): BufferedImage {
    val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics2D = resizedImage.createGraphics()
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    graphics2D.drawImage(this, 0, 0, width, height, null)
    graphics2D.dispose()
    return resizedImage
}

fun BufferedImage.drawText_x(x: Int, y: Int, text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    graphics.drawString(text, x, y)
    graphics.dispose()
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    return Rectangle(x, y, width, height)
}
// 複数行のテキストを描画する
fun BufferedImage.drawText(x: Int, y: Int, text: List<String>, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    val metrics = graphics.fontMetrics
    val height = metrics.height
    var y1 = y
    for (line in text) {
        graphics.drawString(line, x, y1)
        y1 += height
    }
    graphics.dispose()
    return Rectangle(x, y, 0, y1 - y)
}
// 改行を含むテキストを描画する
fun BufferedImage.drawText(x: Int, y: Int, text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    val metrics = graphics.fontMetrics
    val height = metrics.height
    val lines = text.split("\\n")
    var y1 = y
    for (line in lines) {
        graphics.drawString(line, x, y1)
        y1 += height
    }
    graphics.dispose()
    return Rectangle(x, y, 0, y1 - y)
}


fun BufferedImage.drawPolygon(xPoints: IntArray, yPoints: IntArray, color: Color): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.drawPolygon(Polygon(xPoints, yPoints, xPoints.size))
    graphics.dispose()

    val minX = xPoints.minOrNull() ?: 0
    val minY = yPoints.minOrNull() ?: 0
    val maxX = xPoints.maxOrNull() ?: 0
    val maxY = yPoints.maxOrNull() ?: 0

    return Rectangle(minX, minY, maxX - minX, maxY - minY)
}

fun BufferedImage.fillPolygon(xPoints: IntArray, yPoints: IntArray, color: Color): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.fillPolygon(Polygon(xPoints, yPoints, xPoints.size))
    graphics.dispose()

    val minX = xPoints.minOrNull() ?: 0
    val minY = yPoints.minOrNull() ?: 0
    val maxX = xPoints.maxOrNull() ?: 0
    val maxY = yPoints.maxOrNull() ?: 0

    return Rectangle(minX, minY, maxX - minX, maxY - minY)
}
fun BufferedImage.drawTextCenter(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = (this.width - width) / 2
    val y = (this.height - height) / 2
    // textを複数行に分割
    val lines = text.split("\\n")
    var y1 = y
    for (line in lines) {
        val width = metrics.stringWidth(line)
        graphics.drawString(line, (this.width - width) / 2, y1)
        y1 += height
    }

    //graphics.drawString(text, x, y)

    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextLeft(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = 0
    val y = (this.height - height) / 2
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextRight(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = this.width - width
    val y = (this.height - height) / 2
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextTopLeft(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = 0
    val y = metrics.height
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextTopRight(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = this.width - width
    val y = metrics.height
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextTop(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = (this.width - width) / 2
    val y = height
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextBottom(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = (this.width - width) / 2
    val y = this.height - height
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextBottomRight(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = this.width - width
    val y = this.height - height
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawTextBottomLeft(text: String, size: Float = 13.0f, color: Color = Color.WHITE): Rectangle {
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.font = graphics.font.deriveFont(size)
    // 描画サイズを求める
    val metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text)
    val height = metrics.height
    val x = 0
    val y = this.height - height
    graphics.drawString(text, x, y)
    graphics.dispose()
    return Rectangle(x, y, width, height)
}

fun BufferedImage.drawImageCenter(image: BufferedImage): Rectangle {
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
    return Rectangle(x, y, maxWidth, maxHeight)
}

fun BufferedImage.drawImage(image: BufferedImage, x: Int = 0, y: Int = 0, w: Int = -1, h: Int = -1): Rectangle {

    // 元画像のサイズ
    val sourceWidth = image.width
    val sourceHeight = image.height

    var width = w
    var height = h
    if (width == -1)
        width = sourceWidth
    if (height == -1)
        height = sourceHeight

    this.graphics.drawImage(image, x, y, width, height, null)
    return Rectangle(x, y, width, height)
}


fun BufferedImage.drawImageFully(image: BufferedImage): Rectangle {
    // 描画先の画像サイズ
    val targetWidth = this.width
    val targetHeight = this.height

    // 元画像のサイズ
    val sourceWidth = image.width
    val sourceHeight = image.height

    // アスペクト比を維持しながら全体が表示されるように調整
    val aspect = sourceWidth.toDouble() / sourceHeight.toDouble()
    val newWidth: Int
    val newHeight: Int
    if (targetWidth / aspect >= targetHeight) {
        // 高さに合わせる場合
        newHeight = targetHeight
        newWidth = (newHeight * aspect).toInt()
    } else {
        // 幅に合わせる場合
        newWidth = targetWidth
        newHeight = (newWidth / aspect).toInt()
    }

    // 中央に描画
    val x = (targetWidth - newWidth) / 2
    val y = (targetHeight - newHeight) / 2

    this.graphics.drawImage(image, x, y, newWidth, newHeight, null)
    return Rectangle(x, y, newWidth, newHeight)
}

fun BufferedImage.drawImageNoMargin(image: BufferedImage, x: Int = 0, y: Int = 0): Rectangle {
    // 描画先の画像サイズ
    val targetWidth = this.width
    val targetHeight = this.height

    // 元画像のサイズ
    val sourceWidth = image.width
    val sourceHeight = image.height

    // アスペクト比を維持しながら全体が表示されるように調整
    val aspect = sourceWidth.toDouble() / sourceHeight.toDouble()
    val newWidth: Int
    val newHeight: Int
    if (targetWidth / aspect >= targetHeight) {
        // 高さに合わせる場合
        newHeight = targetHeight
        newWidth = (newHeight * aspect).toInt()
    } else {
        // 幅に合わせる場合
        newWidth = targetWidth
        newHeight = (newWidth / aspect).toInt()
    }
    val g = this.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    // 余白なしで描画
    g.drawImage(image, x, y, newWidth, newHeight, null)
    g.dispose()
    return Rectangle(0, 0, newWidth, newHeight)
}

fun BufferedImage.drawImageStretch(image: BufferedImage, x: Int = 0, y: Int = 0, w: Int = 0, h: Int = 0): Rectangle {
    val g = this.createGraphics()

    if (w != 0 && h != 0) {
        g.drawImage(image, x, y, w, h, null)
        g.dispose()
        return Rectangle(x, y, w, h)
    }

    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(image, x, y, this.width, this.height, null)
    g.dispose()
    return Rectangle(0, 0, this.width, this.height)
}

fun BufferedImage.drawLine(x1: Int, y1: Int, x2: Int, y2: Int, radius: Int, color: Color): Rectangle {
    // 色とペンの太さを設定する
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.stroke = BasicStroke(radius.toFloat())

    graphics.drawLine(x1, y1, x2, y2)
    graphics.dispose()
    // 線の太さも考慮して矩形を返す
    val dx = (x2 - x1).absoluteValue
    val dy = (y2 - y1).absoluteValue
    val x = min(x1, x2)
    val y = min(y1, y2)

    return Rectangle(x - radius, y - radius, dx + radius, dy + radius)
}

fun BufferedImage.drawFillCircle(x: Int, y: Int, radius: Int, color: Color): Rectangle {
    // 円を描画
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2)
    graphics.dispose()
    return Rectangle(x - radius, y - radius, radius * 2, radius * 2)
}

fun BufferedImage.drawCircle(x: Int, y: Int, radius: Int, color: Color): Rectangle {
    // 円を描画
    val graphics = this.createGraphics()
    graphics.color = color
    graphics.drawOval(x - radius, y - radius, radius * 2, radius * 2)
    graphics.dispose()
    return Rectangle(x - radius, y - radius, radius * 2, radius * 2)
}