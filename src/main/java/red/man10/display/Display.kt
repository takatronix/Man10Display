package red.man10.display

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import java.awt.image.BufferedImage
import java.util.function.Consumer

// minecraft map size
private const val mapWidth = 128
private const val mapHeight = 128

open class Display {
    var name:String = ""
    var mapIds = mutableListOf<Int>()
    var width: Int = 1
    var height: Int = 1
    var location: Location? = null
    var bufferedImage: BufferedImage? = null
    private var mapCache = mutableListOf<ByteArray?>()
    private var sentMapCount: Long = 0
    open val imageWidth: Int
        get() = width * mapWidth
    open val imageHeight: Int
        get() = height * mapHeight
    private val mapCount: Int
        get() = width * height

    constructor(name: String, width: Int, height: Int) {
        this.name = name
        this.width = width
        this.height = height
        init()
    }

    constructor(config: YamlConfiguration, name: String) {
        this.load(config, name)
        init()
    }

    private fun init() {
        this.bufferedImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
        for (i in 0 until this.mapCount) {
            mapCache.add(null)
        }
    }
    open fun deinit(){
        bufferedImage?.flush()
        mapCache.clear()
    }

    fun updateMapCache() {
        bufferedImage?.let { image ->
            var index = 0
            for (y in 0 until image.height step mapHeight) {
                for (x in 0 until image.width step mapWidth) {
                    val tileImage = image.getSubimage(x, y, mapWidth, mapHeight)
                    val tileBytes = MapPalette.imageToBytes(tileImage)
                    mapCache[index] = tileBytes
                    index++
                }
            }
        }
    }

    private fun createMapPacket(mapId: Int, data: ByteArray?): PacketContainer {
        if (data == null) {
            throw NullPointerException("data is null")
        }
        val packet = PacketContainer(PacketType.Play.Server.MAP)
        val packetModifier = packet.modifier
        packetModifier.writeDefaults()
        val packetIntegers = packet.integers
        if (packetModifier.size() > 5) {
            packetIntegers.write(1, 0).write(2, 0).write(3, 128).write(4, 128)
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

    fun sendMapData(player: Player, mapId: Int, mapData: ByteArray) {

        val packet = createMapPacket(mapId, mapData)
        // Send the packet to the player
        try {
            Main.protocolManager.sendServerPacket(player, packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMapPacketsToPlayers() {

        for (player in Bukkit.getOnlinePlayers()) {
            for (i in 0 until mapCount) {
                val mapData = mapCache.getOrNull(i) ?: continue
                sendMapData(player, mapIds[i], mapData)
                sentMapCount++
            }
        }

        /*
        val protocolManager = ProtocolLibrary.getProtocolManager()

        try{
            val packets = mapIds.mapIndexedNotNull { index, mapId ->
                val mapData = mapCache.getOrNull(index) ?: return@mapIndexedNotNull null
                val mapPacket = protocolManager.createPacket(PacketType.Play.Server.MAP)
                mapPacket.integers.write(0, mapId)
                mapPacket.byteArrays.write(0, mapData)
                mapPacket
            }


            for (player in Bukkit.getOnlinePlayers()) {
                for (packet in packets) {
                    try {
                        protocolManager.sendServerPacket(player, packet)
                        sentMapCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }



        }catch (E:Exception){
            E.printStackTrace()
        }
*/
    }


        /*
        val protocolManager = ProtocolLibrary.getProtocolManager()

        try{
            val packets = mapIds.mapIndexedNotNull { index, mapId ->
                val mapData = mapCache.getOrNull(index) ?: return@mapIndexedNotNull null
                val mapPacket = protocolManager.createPacket(PacketType.Play.Server.MAP)
                mapPacket.integers.write(0, mapId)
                mapPacket.byteArrays.write(0, mapData)
                mapPacket
            }


            for (player in Bukkit.getOnlinePlayers()) {
                for (packet in packets) {
                    try {
                        protocolManager.sendServerPacket(player, packet)
                        sentMapCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }



        }catch (E:Exception){
            E.printStackTrace()
        }
*/


    open fun save(config: YamlConfiguration, path: String) {
        config.set("$path.class", javaClass.simpleName)
        config.set("$path.name", name)
        config.set("$path.mapIdList", mapIds)
        config.set("$path.width", width)
        config.set("$path.height", height)
        // Location情報を保存
        location?.let { loc ->
            config.set("$path.location.world", loc.world?.name)
            config.set("$path.location.x", loc.x)
            config.set("$path.location.y", loc.y)
            config.set("$path.location.z", loc.z)
            config.set("$path.location.yaw", loc.yaw)
            config.set("$path.location.pitch", loc.pitch)
        }
    }

    open fun load(config: YamlConfiguration, key: String) {
        mapIds = (config.getIntegerList("$key.mapIds") ?: mutableListOf()).toMutableList()
        name = config.getString("$key.name") ?: ""
        width = config.getInt("$key.width")
        height = config.getInt("$key.height")

        // load location data
        val worldName = config.getString("$key.location.world")
        val x = config.getDouble("$key.location.x")
        val y = config.getDouble("$key.location.y")
        val z = config.getDouble("$key.location.z")
        val yaw = config.getDouble("$key.location.yaw").toFloat()
        val pitch = config.getDouble("$key.location.pitch").toFloat()

        if (worldName != null) {
            val world = Bukkit.getWorld(worldName)
            location = Location(world, x, y, z, yaw, pitch)
        }
    }
}

class ImageDisplay(name: String, width: Int, height: Int) : Display(name,width,height) {
    var imageUrl = ""

    override fun save(config: YamlConfiguration, path: String) {
        super.save(config, path)
        config.set("$path.imageUrl", imageUrl)
    }

    override fun load(config: YamlConfiguration, path: String) {
        super.load(config, path)
        imageUrl = config.getString("$path.imageUrl") ?: ""
    }
}

class StreamDisplay: Display{
    private var port: Int = 0
    private var videoCaptureServer = VideoCaptureServer(port)

    constructor(name: String, width: Int, height: Int, port: Int) : super(name, width, height){
        this.port = port
        startServer()
    }
    constructor(config: YamlConfiguration,name:String) : super(config,name){
        this.load(config,name)
        startServer()
    }

    private fun startServer(){
        videoCaptureServer.onFrame(Consumer { image ->
            this.bufferedImage = image
            this.updateMapCache()
            this.sendMapPacketsToPlayers()
        })
        info("§e§l [Man10Display] server started on port $port")
    }

    override fun deinit() {
        videoCaptureServer.deinit()
    }

    override fun save(config: YamlConfiguration, path: String) {
        super.save(config, path)
        config.set("$path.port", port)
    }

    override fun load(config: YamlConfiguration, path: String) {
        super.load(config, path)
        port = config.getInt("$path.port")
    }
}
