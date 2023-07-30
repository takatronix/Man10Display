package red.man10.extention

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Sound
import org.bukkit.entity.Player


fun Player.playSound(soundId: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
    val sound = try {
        Sound.valueOf(soundId)
    } catch (e: IllegalArgumentException) {
        // サウンドIDが無効な場合の処理
        println("Invalid sound ID: $soundId")
        return
    }

    this.playSound(this.location, sound, volume, pitch)
}

fun Player.setActionText(message: String) {
    if (message.isEmpty())
        return
    if (player == null)
        return
    if (!this.isOnline)
        return
    val component = TextComponent.fromLegacyText(message)
    this.spigot().sendMessage(ChatMessageType.ACTION_BAR, component[0])
}

fun Player.sendClickableMessage(message: String, command: String) {
    val textComponent = TextComponent(message)
    textComponent.color = ChatColor.GOLD
    textComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
    spigot().sendMessage(textComponent)
}
fun Player.showModeTitle(title: String, subtitle: String = "", keepSec: Double) {
    val time = keepSec * 20.0
    sendTitle(title, subtitle, 10, time.toInt(), 10)
}