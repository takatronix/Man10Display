package red.man10.display

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import red.man10.display.filter.*
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

// minecraft map size
private const val MC_MAP_SIZE_X = 128
private const val MC_MAP_SIZE_Y = 128
//
const val DEFAULT_DISTANCE = 32.0
abstract class Display<DitheringProcessor> {
    var name: String = ""
    var mapIds = mutableListOf<Int>()
    var width: Int = 1
    var height: Int = 1
    var location: Location? = null
    var distance = DEFAULT_DISTANCE

    var bufferedImage: BufferedImage? = null
    var modified = false

    // filter settings
    var testMode = false
    var keepAspectRatio = false
    var aspectRatioWidth = 16.0
    var aspectRatioHeight = 9.0
    var originalWidth = 0
    var originalHeight = 0
    var dithering = false
    var fastDithering = false
    var invert = false
    var showStatus = false
    var monochrome = false
    var sepia = false
    var flip = false
    var colorEnhancer = false
    var saturationLevel = 1.0
    var noiseLevel = DEFAULT_NOISE_LEVEL
    var noise = false
    var sobelLevel: Int = 100
    var sobel = false
    var quantize = false
    var quantizeLevel: Int = 6
    var cartoon = false
    var denoise = false
    var denoiseRadius = 2
    var contrast = false
    var contrastLevel = 1.5
    var scanline = false
    var scanlineWidth = DEFAULT_SCANLINE_HEIGHT
    var sharpen = false
    var sharpenLevel = DEFAULT_SHARPEN_LEVEL
    var blur = false
    var blurRadius = DEFAULT_BLUR_RADIUS
    var brightness = false
    var brightnessLevel = DEFAULT_BRIGHTNESS_LEVEL

    private var mapCache = mutableListOf<ByteArray?>()
    private var refreshPeriod: Long = (1000 / 20) //画面更新サイクル(ms) 20 ticks per second(50ms)

    // statistics
    var sentMapCount: Long = 0
    var refreshCount: Long = 0
    var sentBytes: Long = 0
    var lastCacheTime: Long = 0
    var frameReceivedCount: Long = 0
    var frameReceivedTime: Long = 0
    var startTime: Long = System.currentTimeMillis()
    var lastPacketSentTime: Long = System.currentTimeMillis()
    var lastEffectTime: Long = System.currentTimeMillis()

    fun resetStats() {
        refreshCount = 0
        sentMapCount = 0
        sentBytes = 0
        lastCacheTime = 0
        frameReceivedCount = 0
        frameReceivedTime = 0
        startTime = System.currentTimeMillis()
    }

    open val imageWidth: Int
        get() = width * MC_MAP_SIZE_X
    open val imageHeight: Int
        get() = height * MC_MAP_SIZE_Y
    private val mapCount: Int
        get() = width * height
    private val currentFPS: Double
        get() = refreshCount.toDouble() / (System.currentTimeMillis() - startTime) * 1000
    val fps: Double
        get() = 1000 / refreshPeriod.toDouble()
    private val mps: Double
        get() = sentMapCount.toDouble() / (System.currentTimeMillis() - startTime) * 1000
    private val bps: Long
        get() = (sentBytes.toDouble() / (System.currentTimeMillis() - startTime) * 1000).toLong() * 8

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
        startSendingPacketsTask()
    }

    open fun deinit() {
        bufferedImage?.flush()
        mapCache.clear()
        stopSendingPacketsTask()
    }

    fun updateMapCache() {
        bufferedImage?.let { image ->
            lastCacheTime = measureTimeMillis {
                var index = 0
                for (y in 0 until image.height step MC_MAP_SIZE_Y) {
                    for (x in 0 until image.width step MC_MAP_SIZE_X) {
                        val tileImage = image.getSubimage(x, y, MC_MAP_SIZE_X, MC_MAP_SIZE_Y)
                        val tileBytes = MapPalette.imageToBytes(tileImage)
                        mapCache[index] = tileBytes
                        index++
                    }
                }

            }
        }
    }


    // region draw functions
    fun drawText(text: String, x: Int, y: Int, color: Int) {
        bufferedImage?.let { image ->
            val graphics = image.graphics
            graphics.color = java.awt.Color(color)
            graphics.drawString(text, x, y)
            modified = true
        }
    }
    // endregion

    // region config
    open fun save(config: YamlConfiguration, key: String) {
        config.set("$key.class", javaClass.simpleName)
        config.set("$key.mapIds", mapIds)
        config.set("$key.width", width)
        config.set("$key.height", height)
        config.set("$key.refreshPeriod", refreshPeriod)
        config.set("$key.testMode", testMode)
        config.set("$key.keepAspectRatio", keepAspectRatio)
        config.set("$key.aspectRatioWidth", aspectRatioWidth)
        config.set("$key.aspectRatioHeight", aspectRatioHeight)
        config.set("$key.dithering", dithering)
        config.set("$key.fastDithering", fastDithering)
        config.set("$key.showStatus", showStatus)
        config.set("$key.monochrome", monochrome)
        config.set("$key.sepia", sepia)
        config.set("$key.colorEnhancer", colorEnhancer)
        config.set("$key.flip", flip)
        config.set("$key.invert", invert)
        config.set("$key.saturationLevel", saturationLevel)
        config.set("$key.noiseLevel", noiseLevel)
        config.set("$key.noise", noise)
        config.set("$key.sobelLevel", sobelLevel)
        config.set("$key.sobel", sobel)
        config.set("$key.quantize", quantize)
        config.set("$key.quantizeLevel", quantizeLevel)
        config.set("$key.cartoon", cartoon)
        config.set("$key.blur", blur)
        config.set("$key.blurRadius", blurRadius)
        config.set("$key.denoise", denoise)
        config.set("$key.denoiseRadius", denoiseRadius)
        config.set("$key.contrast", contrast)
        config.set("$key.contrastLevel", contrastLevel)
        config.set("$key.scanline", scanline)
        config.set("$key.scanlineWidth", scanlineWidth)
        config.set("$key.sharpen", sharpen)
        config.set("$key.sharpenLevel", sharpenLevel)
        config.set("$key.brightness", brightness)
        config.set("$key.brightnessLevel", brightnessLevel)
        config.set("$key.distance", distance)

        // save locaiton data
        location?.let { loc ->
            config.set("$key.location.world", loc.world?.name)
            config.set("$key.location.x", loc.x)
            config.set("$key.location.y", loc.y)
            config.set("$key.location.z", loc.z)
            config.set("$key.location.yaw", loc.yaw)
            config.set("$key.location.pitch", loc.pitch)
        }
    }

    open fun load(config: YamlConfiguration, key: String) {
        name = key
        mapIds = config.getIntegerList("$key.mapIds").toMutableList()
        width = config.getInt("$key.width")
        height = config.getInt("$key.height")
        refreshPeriod = config.getLong("$key.refreshPeriod")
        if (refreshPeriod == 0L) {
            refreshPeriod = (1000 / 20)
        }
        testMode = config.getBoolean("$key.testMode", false)
        keepAspectRatio = config.getBoolean("$key.keepAspectRatio", false)
        aspectRatioWidth = config.getDouble("$key.aspectRatioWidth", 16.0)
        aspectRatioHeight = config.getDouble("$key.aspectRatioHeight", 9.0)
        dithering = config.getBoolean("$key.dithering", false)
        fastDithering = config.getBoolean("$key.fastDithering", false)
        showStatus = config.getBoolean("$key.showStatus", false)
        monochrome = config.getBoolean("$key.monochrome", false)
        sepia = config.getBoolean("$key.sepia", false)
        flip = config.getBoolean("$key.flip", false)
        invert = config.getBoolean("$key.invert", false)
        colorEnhancer = config.getBoolean("$key.colorEnhancer", false)
        saturationLevel = config.getDouble("$key.saturationLevel", 1.0)
        noiseLevel = config.getDouble("$key.noiseLevel", 0.0)
        noise = config.getBoolean("$key.noise", false)
        sobelLevel = config.getInt("$key.sobelLevel", DEFAULT_SOBEL_LEVEL)
        sobel = config.getBoolean("$key.sobel", false)
        quantize = config.getBoolean("$key.quantize", false)
        quantizeLevel = config.getInt("$key.quantizeLevel", DEFAULT_QUANTIZE_LEVEL)
        cartoon = config.getBoolean("$key.cartoon", false)
        blur = config.getBoolean("$key.blur", false)
        blurRadius = config.getInt("$key.blurRadius", DEFAULT_BLUR_RADIUS)
        denoise = config.getBoolean("$key.denoise", false)
        denoiseRadius = config.getInt("$key.denoiseRadius", DEFAULT_DENOISE_RADIUS)
        contrast = config.getBoolean("$key.contrast", false)
        contrastLevel = config.getDouble("$key.contrastLevel", DEFAULT_CONTRAST_LEVEL)
        scanline = config.getBoolean("$key.scanline", false)
        scanlineWidth = config.getInt("$key.scanlineWidth", DEFAULT_SCANLINE_HEIGHT)
        sharpen = config.getBoolean("$key.sharpen", false)
        sharpenLevel = config.getDouble("$key.sharpenLevel", DEFAULT_SHARPEN_LEVEL)
        brightness = config.getBoolean("$key.brightness", false)
        brightnessLevel = config.getDouble("$key.brightnessLevel", DEFAULT_BRIGHTNESS_LEVEL)
        distance = config.getDouble("$key.distance", DEFAULT_DISTANCE)

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

    private fun setInterval(sender: CommandSender, intervalSeconds: Double) {
        resetStats()
        refreshPeriod = (intervalSeconds * 1000).toLong()
        sender.sendMessage("intervalSeconds: $intervalSeconds")
        sender.sendMessage("refreshPeriod(ms): $refreshPeriod")
        if (future != null) {
            stopSendingPacketsTask()
            startSendingPacketsTask()
        }
    }

    fun setFps(sender: CommandSender, fps: Double) {
        val intervalSeconds = 1.0 / fps
        setInterval(sender, intervalSeconds)
    }

    fun set(sender: CommandSender, key: String, value: String): Boolean {
        when (key) {
            "interval" -> {
                val intervalSeconds = value.toDoubleOrNull()
                if (intervalSeconds == null) {
                    sender.sendMessage("§cInvalid value: $value")
                    return false
                }
                setInterval(sender, intervalSeconds)
                sender.sendMessage("§aSet interval to $intervalSeconds seconds")
            }

            "fps" -> {
                val fps = value.toDoubleOrNull()
                if (fps == null) {
                    sender.sendMessage("§cInvalid value: $value")
                    return false
                }
                setFps(sender, fps)
                sender.sendMessage("§aSet fps to $fps")
            }

            "refresh" -> {
                resetStats()
            }

            "dithering" -> {
                this.dithering = value.toBoolean()
                this.fastDithering = false
            }

            "fast_dithering" -> {
                this.fastDithering = value.toBoolean()
                this.dithering = false
            }

            "show_status" -> {
                this.showStatus = value.toBoolean()
            }

            "monochrome" -> {
                this.monochrome = value.toBoolean()
            }

            "sepia" -> {
                this.sepia = value.toBoolean()
            }

            "flip" -> {
                this.flip = value.toBoolean()
            }

            "invert" -> {
                this.invert = value.toBoolean()
            }

            "keep_aspect_ratio" -> {
                this.keepAspectRatio = value.toBoolean()
            }

            "aspect_ratio_width" -> {
                this.aspectRatioWidth = value.toDouble()
            }

            "aspect_ratio_height" -> {
                this.aspectRatioHeight = value.toDouble()
            }

            "test_mode" -> {
                this.testMode = value.toBoolean()
            }

            "color_enhancer" -> {
                this.colorEnhancer = value.toBoolean()
            }

            "saturation_factor" -> {
                this.saturationLevel = value.toDouble()
            }

            "noise" -> {
                this.noise = value.toBoolean()
            }

            "noise_level" -> {
                this.noiseLevel = value.toDouble()
            }

            "sobel" -> {
                this.sobel = value.toBoolean()
            }

            "sobel_level" -> {
                this.sobelLevel = value.toInt()
            }

            "quantize" -> {
                this.quantize = value.toBoolean()
            }

            "quantize_level" -> {
                this.quantizeLevel = value.toInt()
            }

            "cartoon" -> {
                this.cartoon = value.toBoolean()
            }

            "blur" -> {
                this.blur = value.toBoolean()
            }

            "blur_radius" -> {
                this.blurRadius = value.toInt()
            }

            "denoise" -> {
                this.denoise = value.toBoolean()
            }

            "denoise_radius" -> {
                this.denoiseRadius = value.toInt()
            }

            "contrast" -> {
                this.contrast = value.toBoolean()
            }

            "contrast_level" -> {
                this.contrastLevel = value.toDouble()
            }

            "brightness" -> {
                this.brightness = value.toBoolean()
            }

            "brightness_level" -> {
                this.brightnessLevel = value.toDouble()
            }

            "scanline" -> {
                this.scanline = value.toBoolean()
            }

            "scanline_width" -> {
                this.scanlineWidth = value.toInt()
            }

            "sharpen" -> {
                this.sharpen = value.toBoolean()
            }

            "sharpen_level" -> {
                this.sharpenLevel = value.toDouble()
            }

            "distance" -> {
                this.distance = value.toDouble()
            }

            else -> {
                sender.sendMessage("§cInvalid key: $key")
                return false
            }
        }

        this.modified = true
        return true
    }
    // endregion

    // region: MapPacketSender
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var future: ScheduledFuture<*>? = null

    private fun startSendingPacketsTask() {
        stopSendingPacketsTask()  // 既にパケットを送信している場合は停止する
        info("startSendingPacketsTask $refreshPeriod ms")
        future = executor.scheduleAtFixedRate(
            {
                if (!this.modified)
                    return@scheduleAtFixedRate
                this.modified = false
                sendMapPacketsToPlayers()
                refreshCount++
            }, 0, refreshPeriod, TimeUnit.MILLISECONDS
        )
    }

    private fun stopSendingPacketsTask() {
        future?.cancel(false)
        future = null
        info("$name stopSendingPacketsTask")
    }

    fun getInfo(): Array<String> {
        val curFps = String.format("%.1f", currentFPS).toDouble()
        val fps = String.format("%.1f", this.fps).toDouble()
        val mps = formatNumberWithCommas(sentMapCount)
        val bps = formatNumberWithCommas(this.bps)
        val totalSent = formatNumberWithCommas(sentBytes)

        return arrayOf(
            "$name($width,$height)",
            "fps:$curFps/$fps",
            "mps:$mps total:$sentMapCount",
            "bps:$bps total:$totalSent",
            "lastCacheTime: $lastCacheTime",
            "lastEffectTime: $lastEffectTime",
        )
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

    private fun sendMapData(player: Player, mapId: Int, mapData: ByteArray) {

        val packet = createMapPacket(mapId, mapData)
        // Send the packet to the player
        try {
            Main.protocolManager.sendServerPacket(player, packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMapPacketsToPlayers_x() {
        for (player in Bukkit.getOnlinePlayers()) {
            for (i in 0 until mapCount) {
                val mapData = mapCache.getOrNull(i) ?: continue
                sendMapData(player, mapIds[i], mapData)
                sentMapCount++
            }
        }
    }

    private fun sendMapPacketsToPlayers() {
        // Convert cache of display to packet list
        val packets = mapIds.mapIndexedNotNull { index, mapId ->
            val mapData = mapCache.getOrNull(index) ?: return@mapIndexedNotNull null
            sentBytes += mapData.size
            val mapPacket = createMapPacket(mapId, mapData)
            mapPacket
        }

        lastPacketSentTime = measureTimeMillis {
            for (player in Bukkit.getOnlinePlayers()) {
                for (packet in packets) {
                    try {
                        Main.protocolManager.sendServerPacket(player, packet)
                        sentMapCount++

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    // endregion
}