package red.man10.display

import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapPalette
import org.bukkit.map.MapView
import red.man10.display.Main.Companion.imageManager
import red.man10.display.macro.MacroCommand
import red.man10.extention.drawImageFully
import java.awt.image.BufferedImage

class App(val mapId: Int, val player: Player,val key: String) :  Display() {

    var globalMapId: Int = -1
    var localMapId: Int = -1

    init{
        this.width = 1
        this.height = 1
        super.init()
    }


    companion object{

    }
    override fun getTargetPlayers(): List<Player> {
        return listOf(player)
    }




    override fun getMessagePlayers(): List<Player> {
        val players = mutableListOf<Player>()
        this.playersCount = Bukkit.getOnlinePlayers().size
        for (p in Bukkit.getOnlinePlayers()) {
            if (this.message_distance > 0.0) {
                if (player.location.world != p.world)
                    continue
                if (p.location.distance(player.location) > message_distance)
                    continue
            }
            if (p.isOnline) {
                players.add(p)
            }
        }
        return players
    }

    override fun getSoundPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        this.playersCount = Bukkit.getOnlinePlayers().size
        for (p in Bukkit.getOnlinePlayers()) {
            if (this.message_distance > 0.0) {
                if (player.location.world != p.world)
                    continue
                if (p.location.distance(player.location) > sound_distance)
                    continue
            }
            if (p.isOnline) {
                players.add(p)
            }
        }
        return players
    }


    override fun createPacketCache(image: BufferedImage, key: String, send: Boolean) {

        val packets = mutableListOf<PacketContainer>()
        val bytes = MapPalette.imageToBytes(image)
        val packet = MapPacketSender.createMapPacket(mapId, bytes)
        packets.add(packet)

        packetCache[key] = packets

        if(send){
            sendMapCache(getTargetPlayers(), key)
        }
    }

    private var  thread :Thread? =null
    private var threadExit = false
    fun startImageTask(imagePath: String,player: Player) {

        this.thread = Thread(Runnable {
            //info("image thread start $imagePath",player)
            while (!this.threadExit) {
                threadExit = true

                val cache = packetCache[imagePath]
                if(cache != null){
                    sendMapCache(getTargetPlayers(), imagePath)
                    return@Runnable
                }

                val image = ImageLoader.get(imagePath)
                if(image != null){
                    this.currentImage?.drawImageFully(image)
                    this.createPacketCache(this.currentImage!!, imagePath, true)
                }else{
                    this.sendMapCache(getTargetPlayers(), "blank")
                }

            }
            //info("image thread exit $imagePath",player)
        })
        this.thread!!.start()
    }

    override fun deinit(){
        this.macroEngine.stop()
        this.clearCache()
    }
}