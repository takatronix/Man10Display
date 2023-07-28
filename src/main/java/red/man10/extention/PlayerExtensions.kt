import org.bukkit.FluidCollisionMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult

/**
 * プレイヤーの視線の先にあるブロックとヒットしたブロックの面を取得します。
 *
 * @param range ブロックをチェックする最大距離。
 * @return プレイヤーの視線の先にあるブロックとヒットしたブロックの面の情報をRayTraceResultで返します。
 *         範囲内にブロックが見つからなかった場合はnullを返します。
 */
fun Player.rayTraceBlocks(range: Double): RayTraceResult? {
    return world.rayTraceBlocks(
        eyeLocation, eyeLocation.direction, range, FluidCollisionMode.NEVER, true
    )
}

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