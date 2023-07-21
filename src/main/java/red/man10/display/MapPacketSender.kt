package red.man10.display

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player
import kotlin.system.measureTimeMillis

public interface  MapPacketSender {

    companion object{
        fun send(players:List<Player>,packets:List<PacketContainer>):Int {
            var sent = 0
            val time = measureTimeMillis {
                for (player in players) {
                    if(!player.isOnline)
                        continue
                    for (packet in packets) {
                        try {
                            Main.protocolManager.sendServerPacket(player, packet)
                            sent ++
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            return sent
        }
        fun createMapPacket(mapId: Int, data: ByteArray?): PacketContainer {
            if (data == null) {
                throw NullPointerException("data is null")
            }
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
        fun sendPackets(players:List<Player>,packets:List<PacketContainer>){
            for (player in players) {
                if(!player.isOnline)
                    continue
                for (packet in packets) {
                    try {
                        Main.protocolManager.sendServerPacket(player, packet)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }


        fun sendMapImage(player: Player,data:ByteArray,mapIds:List<Int>){
            if(!player.isOnline)
                return
            for (mapId in mapIds) {
                val packet = createMapPacket(mapId, data)
                try {
                    Main.protocolManager.sendServerPacket(player, packet)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            info("send map data to ${player.name}")
        }

    }


}