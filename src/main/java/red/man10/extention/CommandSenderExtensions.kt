package red.man10.extention

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender

fun CommandSender.sendClickableMessage(message: String, command: String) {
    val textComponent = TextComponent(message)
    textComponent.color = ChatColor.GOLD
    textComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
    spigot().sendMessage(textComponent)
}

/**
 * プレイヤーにクリック可能なメッセージを送信します。メッセージ内のプレースホルダはクリックするとそのプレースホルダに含まれるコマンドを実行します。
 * player?.sendClickableMessage("§cここはクリックできない {§aクリックメッセージ:/command} ---- {§bnext page:/next}")
 * @param message 送信するメッセージ。メッセージ内のプレースホルダはクリック可能で、クリックするとプレースホルダに含まれるコマンドが実行されます。
 */
fun CommandSender.sendClickableMessage(message: String) {
    val textComponent = TextComponent()

    // プレースホルダを含むパターン
    val pattern = Regex("\\{.*?}")

    // メッセージをプレースホルダで分割
    val parts = pattern.split(message)

    // プレースホルダを見つける
    val placeholders = pattern.findAll(message).toList()

    // メッセージの各部分を処理
    for (i in parts.indices) {
        textComponent.addExtra(translateColorCodes(parts[i]))

        // プレースホルダを含む場合
        if (i < placeholders.size) {
            val placeholder = placeholders[i].value
            val splitPlaceholder = placeholder.substring(1, placeholder.length - 1).split(":")
            val displayText = translateColorCodes(splitPlaceholder[0].trim())
            val command = splitPlaceholder[1].trim()
            val clickableComponent = TextComponent(displayText)
            clickableComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
            textComponent.addExtra(clickableComponent)
        }
    }

    this.spigot().sendMessage(textComponent)
}

/**
 * Minecraftの§プレースホルダーをBungeeCordのChat APIで使用できる形式に変換します。
 *
 * @param message 変換するメッセージ。
 * @return 変換後のメッセージ。
 */
fun translateColorCodes(message: String): String {
    return ChatColor.translateAlternateColorCodes('§', message)
}


