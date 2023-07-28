package red.man10.display

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import red.man10.display.Main.Companion.plugin



/**
 * ベクトル間にラインを引くパーティクルを表示します。
 *
 * @param world パーティクルを表示するワールド。
 * @param start ラインの開始ベクトル。
 * @param end ラインの終了ベクトル。
 * @param color ラインの色。
 * @param density パーティクルの密度。1より大きい値を指定すると密になります。
 * @param duration ラインが表示される時間（ティック単位）。
 */
fun drawLineParticle(world: World, start: Vector, end: Vector, color: Color, density: Int, duration: Int) {
    val direction = end.clone().subtract(start).normalize()
    val length = start.distance(end)
    val steps = (length * density).toInt()

    for (i in 0 until steps) {
        val position = start.clone().add(direction.clone().multiply(length * i / steps))
        world.spawnParticle(Particle.REDSTONE, position.x, position.y, position.z, 0, 0.0, 0.0, 0.0, 0.0,
            Particle.DustOptions(color, 1.0f))
    }

    // 指定された時間後にラインを消すタスクをスケジュールする
    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
        for (i in 0 until steps) {
            val position = start.clone().add(direction.clone().multiply(length * i / steps))
            world.spawnParticle(Particle.REDSTONE, position.x, position.y, position.z, 0, 0.0, 0.0, 0.0, 0.0,
                Particle.DustOptions(Color.BLACK, 1.0f))
        }
    }, duration.toLong())
}

/*
private fun playSleepAnimation(asleep: Player) {
    val bedPacket: PacketContainer = manager.createPacket(PacketType.Play.Server.BED, false)
    val loc = asleep.location

    // http://wiki.vg/Protocol#Use_Bed
    bedPacket.getEntityModifier(asleep.world).write(0, asleep)
    bedPacket.integers.write(1, loc.blockX).write(2, loc.blockY + 1).write(3, loc.blockZ)
    broadcastNearby(asleep, bedPacket)
}*/