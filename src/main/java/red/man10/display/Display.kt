package red.man10.display

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import red.man10.display.MapPacketSender.Companion.createMapPacket
import red.man10.display.MapPacketSender.Companion.sendMapImage
import red.man10.display.filter.*
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

// minecraft map size
const val MC_MAP_SIZE_X = 128
const val MC_MAP_SIZE_Y = 128
//
const val DEFAULT_DISTANCE = 32.0
abstract class Display : MapPacketSender {
    var name: String = ""
    var mapIds = mutableListOf<Int>()
    var width: Int = 1
    var height: Int = 1
    var location: Location? = null
    var distance = DEFAULT_DISTANCE
    var port: Int = 0

    var bufferedImage: BufferedImage? = null

    // filter settings
    var testMode = false
    var keepAspectRatio = false
    var aspectRatioWidth = 16.0
    var aspectRatioHeight = 9.0
    var dithering = false
    var fastDithering = false
    var invert = false
    var showStatus = false
    var monochrome = false
    var sepia = false
    var flip = false
    var colorEnhancer = false
    var saturationLevel = DEFAULT_SATURATION_LEVEL
    var noiseLevel = DEFAULT_NOISE_LEVEL
    var noise = false
    var sobelLevel: Int = DEFAULT_SOBEL_LEVEL
    var sobel = false
    var quantize = false
    var quantizeLevel: Int = DEFAULT_QUANTIZE_LEVEL
    var cartoon = false
    var denoise = false
    var denoiseRadius = DEFAULT_DENOISE_RADIUS
    var contrast = false
    var contrastLevel = DEFAULT_CONTRAST_LEVEL
    var scanline = false
    var scanlineWidth = DEFAULT_SCANLINE_HEIGHT
    var sharpen = false
    var sharpenLevel = DEFAULT_SHARPEN_LEVEL
    var blur = false
    var blurRadius = DEFAULT_BLUR_RADIUS
    var brightness = false
    var brightnessLevel = DEFAULT_BRIGHTNESS_LEVEL
    var parallelDithering = false
    var parallelism = DEFAULT_PARALLELISM


    var refreshPeriod: Long = (1000 / 20) //画面更新サイクル(ms) 20 ticks per second(50ms)

    // statistics
    var lastCacheTime: Long = 0
    var startTime: Long = System.currentTimeMillis()
    var lastPacketSentTime: Long = System.currentTimeMillis()
    var lastFilterTime: Long = System.currentTimeMillis()
    var playersCount: Int = 0
    var sentPlayers = mutableListOf<Player>()
    var refreshCount: Long = 0
    var sentMapCount: Long = 0
    var sentBytes: Long = 0
    var frameReceivedCount: Long = 0
    var frameReceivedBytes: Long = 0
    var frameErrorCount: Long = 0
    var refreshFlag = false

    private fun resetStats() {
        refreshCount = 0
        sentMapCount = 0
        sentBytes = 0
        lastCacheTime = 0
        frameReceivedCount = 0
        frameReceivedBytes = 0
        frameErrorCount = 0
        startTime = System.currentTimeMillis()
        if(this is StreamDisplay){
            this.resetVideoStats()
        }
    }
    var blankMap : ByteArray? = null
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

    val className:String
        get() {
            return this.javaClass.simpleName
        }
    val locInfo:String
        get() {
            return if(location == null){
                "None"
            }else{
                "${location!!.world?.name}(${location!!.x.toInt()},${location!!.y.toInt()},${location!!.z.toInt()})"
            }
        }
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
        val blankImage = BufferedImage(MC_MAP_SIZE_X, MC_MAP_SIZE_Y, BufferedImage.TYPE_INT_RGB)
        blankMap = MapPalette.imageToBytes(blankImage)

        for (i in 0 until this.mapCount) {
            mapCache.add(null)
            mapPackets.add(PacketContainer(PacketType.Play.Server.MAP))
        }
        createBlankDisplayPacket()
        startSendingPacketsTask()
    }
    private fun createBlankDisplayPacket(){
        for(mapId in mapIds){
            createMapPacket(mapId,blankMap!!).let { packet ->
                blankPackets.add(packet)
            }
        }
    }


    open fun deinit() {
        this.refreshFlag = false
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

    // endregion

    companion object{
        val displayTypes:ArrayList<String>
            get() {
                return arrayListOf(
                    "stream","image")
            }

    }
    // region config
    open fun save(config: YamlConfiguration, key: String) {
        config.set("$key.class", className)
        config.set("$key.mapIds", mapIds)
        config.set("$key.width", width)
        config.set("$key.height", height)
        config.set("$key.port", port)
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
        config.set("$key.parallelDithering", parallelDithering)
        config.set("$key.parallelism", parallelism)

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
        port = config.getInt("$key.port")
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
        parallelDithering = config.getBoolean("$key.parallelDithering", false)
        parallelism = config.getInt("$key.parallelism", DEFAULT_PARALLELISM)

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
                this.parallelDithering = false
            }

            "fast_dithering" -> {
                this.fastDithering = value.toBoolean()
                this.dithering = false
                this.parallelDithering = false
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
            "parallel_dithering" -> {
                this.parallelDithering = value.toBoolean()
                this.dithering = false
                this.fastDithering = false
            }
            "parallelism" -> {
                this.parallelism = value.toInt()
            }
            else -> {
                sender.sendMessage("§cInvalid key: $key")
                return false
            }
        }

        this.refreshFlag = true
        return true
    }
    fun filterImage(image:BufferedImage):BufferedImage{
        var result = image
        this.lastFilterTime = measureTimeMillis {
            if (this.flip) {
                result = FlipFilter().apply(result)
            }
            if (this.monochrome) {
                result = GrayscaleFilter().apply(result)
            }
            if (this.sepia) {
                result = SepiaFilter().apply(result)
            }
            if (this.invert) {
                result = InvertFilter().apply(result)
            }
            if (this.keepAspectRatio) {
                result = AspectRatioFilter(this.aspectRatioWidth / this.aspectRatioHeight).apply(result)
            }
            if (this.brightness) {
                result = BrightnessFilter(brightnessLevel).apply(result)
            }
            if (this.colorEnhancer) {
                result = ColorEnhancerFilter(saturationLevel).apply(result)
            }
            if (this.sharpen) {
                result = SharpenFilter(sharpenLevel).apply(result)
            }
            if (this.cartoon) {
                result = CartoonFilter(quantizeLevel, sobelLevel).apply(result)
            }
            if (this.noise) {
                result = NoiseFilter(noiseLevel).apply(result)
            }
            if (this.quantize) {
                result = ColorQuantizeFilter(quantizeLevel).apply(result)
            }
            if (this.sobel) {
                result = SobelFilter(sobelLevel).apply(result)
            }
            if (this.dithering) {
                result = DitheringFilter().apply(result)
            }
            if (this.fastDithering) {
                result = OrderedDitheringFilter().apply(result)
            }
            if (this.denoise) {
                result = DenoiseFilter(denoiseRadius).apply(result)
            }
            if (this.contrast) {
                result = ContrastFilter(contrastLevel).apply(result)
            }
            if (this.blur) {
                result = BlurFilter(blurRadius).apply(result)
            }
            if (this.scanline) {
                result = ScanlineFilter(scanlineWidth).apply(result)
            }
            if(this.parallelDithering){
                result = ParallelDitheringFilter(parallelism).apply(result)
            }
            if (this.testMode) {
                //    result = ParallelDitheringFilter(4).apply(result!!)
            }

            if (this.showStatus) {
                result = showStatus(result)
            }
        }
        return result
    }
    private fun showStatus(image: BufferedImage):BufferedImage{
        val info = getStatistics()
        val graphics = image.graphics
        graphics.color = java.awt.Color.GREEN
        for(i in info.indices){
            graphics.drawString(info[i],4,12 + i * 13)
        }
        graphics.dispose()
        return image
    }

    // endregion

    // region: MapPacketSender
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var future: ScheduledFuture<*>? = null

    fun getStatistics(): Array<String> {
        val curFps = String.format("%.1f", currentFPS).toDouble()
        val fps = String.format("%.1f", this.fps).toDouble()
        val mps =  String.format("%.1f", this.mps)
        val bps = formatNumberWithCommas(this.bps)
        val totalSent = formatNumberWithCommas(sentBytes)

        val receivedBps = formatNumberWithCommas(frameReceivedBytes / (System.currentTimeMillis() - startTime) * 1000)
        val receivedFps = String.format("%.1f", frameReceivedCount.toDouble() / (System.currentTimeMillis() - startTime) * 1000)
        val receivedTotal = formatNumberWithCommas(frameReceivedBytes)

        return arrayOf(
            "$name($width,$height)",
            "fps:$curFps/$fps",
            "players:${sentPlayers.size}/$playersCount",
            "mps:$mps total:$sentMapCount",
            "bps:$bps total:$totalSent(bytes)",
            "lastCacheTime: $lastCacheTime(ms)",
            "lastFilterTime: $lastFilterTime(ms)",
            "receivedFps:$receivedFps",
            "receivedBps:$receivedBps total:$receivedTotal(bytes)",
        )
    }

    private fun getTargetPlayers():List<Player>{
        val players = mutableListOf<Player>()
        this.playersCount = Bukkit.getOnlinePlayers().size
        for (player in Bukkit.getOnlinePlayers()) {
            // check distance
            if(this.location != null && this.distance > 0.0){
                if(this.location!!.world != player.world)
                    continue
                if(player.location.distance(this.location!!) > distance)
                    continue
            }
            if (player.isOnline) {
                players.add(player)
            }
        }
        return players
    }

    private fun startSendingPacketsTask() {
        info("$name stopSendingPacketsTask $refreshPeriod ms" )
        stopSendingPacketsTask()  // 既にパケットを送信している場合は停止する
        info("startSendingPacketsTask $refreshPeriod ms")
        future = executor.scheduleAtFixedRate(
            {
                if (!this.refreshFlag)
                    return@scheduleAtFixedRate
                this.refreshFlag = false
                sendMapPacketsToPlayers()
                refreshCount++
            }, 0, refreshPeriod, TimeUnit.MILLISECONDS
        )
    }

    private fun sendMapPacketsToPlayers() {
        val players = getTargetPlayers()
        val sent = MapPacketSender.send(players, mapPackets)
        this.sentMapCount += sent
        this.sentBytes += sent * MC_MAP_SIZE_X * MC_MAP_SIZE_Y

        // 前回送信して今回送信しないプレイヤーには、ブランクパケットを送ってディスプレイを消す
        for (player in sentPlayers) {
            if (players.contains(player))
                continue
            if(blankMap != null)
                sendMapImage(player, blankMap!!, mapIds)
        }

        this.sentPlayers = players.toMutableList()
    }
    private fun stopSendingPacketsTask() {
        future?.cancel(false)
        future = null
    }

    private var mapCache = mutableListOf<ByteArray?>()
    private var mapPackets = mutableListOf<PacketContainer>()
    private var blankPackets = mutableListOf<PacketContainer>()
    fun mapCacheToPackets(){
        for (i in 0 until mapCache.size) {
            val data = mapCache[i] ?: continue
            val packet = createMapPacket(mapIds[i], data)
            mapPackets[i] = packet
        }
    }
    // endregion
}