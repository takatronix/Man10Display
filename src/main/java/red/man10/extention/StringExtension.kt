package red.man10.extention

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
