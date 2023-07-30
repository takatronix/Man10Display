package red.man10.extention

import java.awt.Color

fun String.formatNumber(value: Long): String {
    val suffixes = listOf("", "K", "M", "G", "T", "P", "E")
    var convertedValue = value.toDouble()
    var suffixIndex = 0

    while (convertedValue >= 1000 && suffixIndex < suffixes.size - 1) {
        convertedValue /= 1000
        suffixIndex++
    }

    val formattedValue = when {
        convertedValue >= 1000 -> "%.0f".format(convertedValue)
        convertedValue >= 100 -> "%.1f".format(convertedValue)
        else -> "%.2f".format(convertedValue)
    }
    return "$formattedValue${suffixes[suffixIndex]}"
}

fun String.formatNumberWithCommas(value: Long): String {
    val formattedValue = formatNumber(value)
    val parts = formattedValue.split(".")
    val integerPartWithCommas = parts[0].chunked(3).joinToString(",")
    return if (parts.size > 1) "$integerPartWithCommas.${parts[1]}" else integerPartWithCommas
}

fun String.toColor(): Color {
    val hex = this.trimStart('#')
    // 16進数からRGB値を取得
    val r = hex.substring(0, 2).toInt(16)
    val g = hex.substring(2, 4).toInt(16)
    val b = hex.substring(4, 6).toInt(16)
    return Color(r, g, b)
}
fun String.fromColor(color: Color): String {
    val r = color.red.toString(16).padStart(2, '0')
    val g = color.green.toString(16).padStart(2, '0')
    val b = color.blue.toString(16).padStart(2, '0')
    return "#$r$g$b"
}
