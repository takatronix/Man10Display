package red.man10.extention

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