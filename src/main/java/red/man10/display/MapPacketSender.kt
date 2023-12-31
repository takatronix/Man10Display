package red.man10.display

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player

interface MapPacketSender {
    companion object {
        fun send(players: List<Player>, packets: List<PacketContainer>): Int {
            //info("send map packet")
            var sent = 0
            for (player in players) {
                if (!player.isOnline)
                    continue
                for (packet in packets) {
                    try {
                        //info("send map packet ${packet.integers.read(0)} to ${player.name}")
                        Main.protocolManager.sendServerPacket(player, packet)
                        sent++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return sent
        }

        fun createMapPacket(mapId: Int, data: ByteArray?): PacketContainer {
            if (data == null) {
                throw NullPointerException("data is null")
            }
            //info("create map packet mapId:$mapId")
            val packet = PacketContainer(PacketType.Play.Server.MAP)
            val packetModifier = packet.modifier
            packetModifier.writeDefaults()
            val packetIntegers = packet.integers
            if (packetModifier.size() > 5) {
                packetIntegers.write(1, 0).write(2, 0).write(3, MC_MAP_SIZE_X).write(4, MC_MAP_SIZE_Y)
                packet.byteArrays.write(0, data)
            } else {
                try {
                    val lastArg = packetModifier.size() - 1
                    packetModifier.write(
                        lastArg, packetModifier.getField(lastArg).type.getConstructor(
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            ByteArray::class.java
                        ).newInstance(0, 0, 128, 128, data)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            packetIntegers.write(0, mapId)
            packet.bytes.write(0, 0.toByte())
            val packetBooleans = packet.booleans
            if (packetBooleans.size() > 0) {
                packetBooleans.write(0, false)
            }
            return packet
        }

    }
}