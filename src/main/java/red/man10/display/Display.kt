package red.man10.display

import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import red.man10.display.MapPacketSender.Companion.createMapPacket
import red.man10.display.CommandType.*
import red.man10.display.filter.*
import red.man10.display.macro.ImageCommand
import red.man10.display.macro.LineCommand
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import red.man10.display.*
import red.man10.display.macro.*


// minecraft map size
const val MC_MAP_SIZE_X = 128
const val MC_MAP_SIZE_Y = 128
//
const val DEFAULT_DISTANCE = 32.0
const val DEFAULT_FPS = 10.0
//
const val DEFAULT_PROTECTION = true

abstract class Display : MapPacketSender  {
    var name: String = ""
    var mapIds = mutableListOf<Int>()
    var width: Int = 1
    var height: Int = 1
    var location: Location? = null
    var distance = DEFAULT_DISTANCE
    var port: Int = 0
    var macroEngine = MacroEngine()
    var protect = DEFAULT_PROTECTION

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
    var rasterNoise = false
    var rasterNoiseLevel = DEFAULT_RASTER_NOISE_LEVEL
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
    var scanlineHeight = DEFAULT_SCANLINE_HEIGHT
    var sharpen = false
    var sharpenLevel = DEFAULT_SHARPEN_LEVEL
    var blur = false
    var blurRadius = DEFAULT_BLUR_RADIUS
    var brightness = false
    var brightnessLevel = DEFAULT_BRIGHTNESS_LEVEL
    var parallelDithering = false
    var parallelism = DEFAULT_PARALLELISM
    var vignette = false
    var vignetteLevel = DEFAULT_VIGNETTE_LEVEL
    var macroName:String? = ""

    var refreshPeriod: Long = (1000 / DEFAULT_FPS.toLong()) //画面更新サイクル(ms) 20 ticks per second(50ms)

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
    var forceRefresh = true

    fun resetStats() {
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

        // 画像バッファイメージ
        this.currentImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
        createPacketCache(currentImage!!,"current")
        // ブランクイメージ
        val blankImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
        createPacketCache(blankImage,"blank")
        // 送信タスク開始
        startSendingPacketsTask()
    }



    open fun deinit() {
        this.refreshFlag = false
        stopSendingPacketsTask()
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
        config.set("$key.protect", protect)
        config.set("$key.macroName", macroName)
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
        config.set("$key.rasterNoise", rasterNoise)
        config.set("$key.rasterNoiseLevel", rasterNoiseLevel)
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
        config.set("$key.scanlineHeight", scanlineHeight)
        config.set("$key.sharpen", sharpen)
        config.set("$key.sharpenLevel", sharpenLevel)
        config.set("$key.brightness", brightness)
        config.set("$key.brightnessLevel", brightnessLevel)
        config.set("$key.distance", distance)
        config.set("$key.parallelDithering", parallelDithering)
        config.set("$key.parallelism", parallelism)
        config.set("$key.vignette", vignette)
        config.set("$key.vignetteLevel", vignetteLevel)

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
        protect = config.getBoolean("$key.protect", DEFAULT_PROTECTION)
        refreshPeriod = config.getLong("$key.refreshPeriod")
        if (refreshPeriod == 0L) {
            refreshPeriod = (1000 / DEFAULT_FPS.toLong())
        }
        testMode = config.getBoolean("$key.testMode", false)
        macroName = config.getString("$key.macroName","")
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
        rasterNoise = config.getBoolean("$key.rasterNoise", false)
        rasterNoiseLevel = config.getInt("$key.rasterNoiseLevel", DEFAULT_RASTER_NOISE_LEVEL)
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
        scanlineHeight = config.getInt("$key.scanlineHeight", DEFAULT_SCANLINE_HEIGHT)
        sharpen = config.getBoolean("$key.sharpen", false)
        sharpenLevel = config.getDouble("$key.sharpenLevel", DEFAULT_SHARPEN_LEVEL)
        brightness = config.getBoolean("$key.brightness", false)
        brightnessLevel = config.getDouble("$key.brightnessLevel", DEFAULT_BRIGHTNESS_LEVEL)
        distance = config.getDouble("$key.distance", DEFAULT_DISTANCE)
        parallelDithering = config.getBoolean("$key.parallelDithering", false)
        parallelism = config.getInt("$key.parallelism", DEFAULT_PARALLELISM)
        vignette = config.getBoolean("$key.vignette", false)
        vignetteLevel = config.getDouble("$key.vignetteLevel", DEFAULT_VIGNETTE_LEVEL)

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
            "protect" -> this.protect = value.toBoolean()


            "test_mode" -> {
                this.testMode = value.toBoolean()
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

            "show_status" -> this.showStatus = value.toBoolean()
            "monochrome" -> this.monochrome = value.toBoolean()
            "sepia" -> this.sepia = value.toBoolean()
            "flip" -> this.flip = value.toBoolean()
            "invert" -> this.invert = value.toBoolean()
            "keep_aspect_ratio" -> this.keepAspectRatio = value.toBoolean()
            "aspect_ratio_width" -> this.aspectRatioWidth = value.toDouble()
            "aspect_ratio_height" -> this.aspectRatioHeight = value.toDouble()
            "color_enhancer" -> this.colorEnhancer = value.toBoolean()
            "saturation_level" -> this.saturationLevel = value.toDouble()
            "noise" -> this.noise = value.toBoolean()
            "noise_level" -> this.noiseLevel = value.toDouble()
            "raster_noise" -> this.rasterNoise = value.toBoolean()
            "raster_noise_level" -> this.rasterNoiseLevel = value.toInt()
            "vignette" -> this.vignette = value.toBoolean()
            "vignette_level" -> this.vignetteLevel = value.toDouble()
            "sobel" -> this.sobel = value.toBoolean()
            "sobel_level" -> this.sobelLevel = value.toInt()
            "quantize" -> this.quantize = value.toBoolean()
            "quantize_level" -> this.quantizeLevel = value.toInt()
            "cartoon" -> this.cartoon = value.toBoolean()
            "blur" -> this.blur = value.toBoolean()
            "blur_radius" -> this.blurRadius = value.toInt()
            "denoise" -> this.denoise = value.toBoolean()
            "denoise_radius" -> this.denoiseRadius = value.toInt()
            "contrast" -> this.contrast = value.toBoolean()
            "contrast_level" -> this.contrastLevel = value.toDouble()
            "brightness" -> this.brightness = value.toBoolean()
            "brightness_level" -> this.brightnessLevel = value.toDouble()
            "scanline" -> this.scanline = value.toBoolean()
            "scanline_height" -> this.scanlineHeight = value.toInt()
            "sharpen" -> this.sharpen = value.toBoolean()
            "sharpen_level" -> this.sharpenLevel = value.toDouble()
            "distance" -> this.distance = value.toDouble()
            "parallel_dithering" -> {
                this.parallelDithering = value.toBoolean()
                this.dithering = false
                this.fastDithering = false
            }
            "parallelism" -> this.parallelism = value.toInt()
            "location" -> {
                if(sender !is Player){
                    sender.sendMessage("§c this command is only available for player")
                    return false
                }
                val loc = sender.location
                this.location = loc
                sender.sendMessage("§aSet location to ${loc.world?.name}(${loc.x.toInt()},${loc.y.toInt()},${loc.z.toInt()})")
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
            if(this.rasterNoise){
                result = RasterNoiseFilter(rasterNoiseLevel).apply(result)
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
                result = ScanlineFilter(scanlineHeight).apply(result)
            }
            if(this.parallelDithering){
                result = ParallelDitheringFilter(parallelism).apply(result)
            }
            if(this.vignette){
                result = VignetteFilter(vignetteLevel).apply(result)
            }
            if (this.testMode) {

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

    // 画面更新タスクを開始する
    private fun startSendingPacketsTask() {
        info("$name stopSendingPacketsTask $refreshPeriod ms" )
        stopSendingPacketsTask()  // 既にパケットを送信している場合は停止する
        info("startSendingPacketsTask $refreshPeriod ms")
        future = executor.scheduleAtFixedRate(
            {
                // 画面更新フラグが立っていれば、画面すべてを更新する
                if (refreshFlag){
                    this.refreshFlag = false
                    sendMapPacketsToPlayers()
                    refreshCount++
                    return@scheduleAtFixedRate
                }
                // 画面の一部を更新する
                if(updateCacheIndexList.isNotEmpty()){
                    sendMapCacheByIndexList(sentPlayers,updateCacheIndexList)
                    updateCacheIndexList = mutableListOf()
                    return@scheduleAtFixedRate
                }

            }, 0, refreshPeriod, TimeUnit.MILLISECONDS
        )
    }

    // プレイヤーにパケットを送る（一番低レベル)
    private fun sendMapPackets(players:List<Player>,packets:List<PacketContainer>){
        val sent = MapPacketSender.send(players, packets)
        this.sentMapCount += sent
        this.sentBytes += sent * MC_MAP_SIZE_X * MC_MAP_SIZE_Y
    }
    // 作成したキャッシュを送信する
    public fun sendMapCache(players:List<Player> ,key:String = "current"){
        if(!packetCache.containsKey(key))
            return
        //info("sendMapCache $key")
        val packets = packetCache[key]!!
        sendMapPackets(players,packets)
    }
    // 作成したキャッシュの一部を送信する(部分更新用)
    private fun sendMapCacheByIndexList(players:List<Player>,indexList:List<Int>,key:String = "current"){
        if(!packetCache.containsKey(key))
            return
        val packets = packetCache[key]!!
        val targetPackets = mutableListOf<PacketContainer>()
        for(index in indexList){
            targetPackets.add(packets[index])
        }
        sendMapPackets(players,targetPackets)
    }
    private fun sendMapPacketsToPlayers() {
        val players = getTargetPlayers()
        sendMapCache(players)
        // 前回送信して今回送信しないプレイヤーには、ブランクパケットを送ってディスプレイを消す
        for (player in sentPlayers) {
            if (players.contains(player))
                continue
            sendMapCache(players,"blank")
        }
        this.sentPlayers = players.toMutableList()
    }
    private fun stopSendingPacketsTask() {
        future?.cancel(false)
        future = null
    }
    private fun sendBlank(){
        sendMapCache(getTargetPlayers(),"blank")
    }


    // region: Cache


    // 現在の表示バッファ
    var currentImage: BufferedImage? = null


    // key/value でマップの情報をキャッシュする
    // "blank" -> ブランクマップ(オフライン時に表示する)
    // "current" -> 現在表示用バッファ
    var packetCache: MutableMap<String, List<PacketContainer>> = ConcurrentHashMap()
    // 更新が必要なマップのindex
    var updateCacheIndexList:MutableList<Int> = mutableListOf()

    // imageからパケット情報を作成する
    fun createPacketCache(image:BufferedImage,key:String = "current"){
        val packets = mutableListOf<PacketContainer>()
        var index = 0
        for (y in 0 until image.height step MC_MAP_SIZE_Y) {
            for (x in 0 until image.width step MC_MAP_SIZE_X) {
                val tileImage = image.getSubimage(x, y, MC_MAP_SIZE_X, MC_MAP_SIZE_Y)
                val tileBytes = MapPalette.imageToBytes(tileImage)
                val packet = createMapPacket(mapIds[index], tileBytes)
                packets.add(packet)
                index++
            }
        }
        packetCache[key] = packets
    }
    fun getSubImageRectByIndex(index:Int):Rectangle{
        val x = index % width * MC_MAP_SIZE_X
        val y = index / width * MC_MAP_SIZE_Y
        return Rectangle(x,y,MC_MAP_SIZE_X,MC_MAP_SIZE_Y)
    }
    // 指定したrectがcurrentImageのサブイメージの範囲内なら更新リストに追加する
    fun update(updateRect:Rectangle? = null,key:String = "current"){
        // 画像のキャッシュを作成
        createPacketCache(this.currentImage!!,key)
        if(updateRect == null){
            updateCacheIndexList = (0 until mapCount).toMutableList()
            return
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val subRect = getSubImageRectByIndex(index)
                if(subRect.intersects(updateRect)){
                    if(!updateCacheIndexList.contains(index)){
                        updateCacheIndexList.add(index)
                    }
                }
            }
        }
        // 更新リストのindexのマップを送信する
        sendMapCacheByIndexList(sentPlayers,updateCacheIndexList,key)
        updateCacheIndexList.clear()
    }
    fun reset(){
        this.currentImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
        createPacketCache(currentImage!!,"current")
        this.update()
        this.sendBlank()
    }
    fun clearCache(){
        packetCache.clear()
    }
    fun refresh(){
        if(forceRefresh){
            // 現在のパケットキャッシュを送信
            sendMapCache(getTargetPlayers())
            refreshCount++
            return
        }
        this.refreshFlag = true
    }

    // endregion
    // region: Macro
    fun runMacro(macroName:String,sender:CommandSender? = null) :Boolean {
        info("runMacro : $macroName", sender)
        macroEngine.runMacroAsync(macroName) { macroCommand, index ->
            //  info("[$macroName]($index)macro execute : ${macroCommand.type}", sender)
            val players = getTargetPlayers()
            try {
                when (macroCommand.type) {
                    IMAGE -> ImageCommand(macroName, macroCommand).run(this, players,sender)
                    LINE -> LineCommand(macroName, macroCommand).run(this, players,sender)
                    COLOR -> ColorCommand(macroName, macroCommand).run(this, players,sender)
                    REFRESH -> RefreshCommand(macroName, macroCommand).run(this, players,sender)
                    CLEAR -> ClearCommand(macroName, macroCommand).run(this, players,sender)
                    FILL -> FillCommand(macroName, macroCommand).run(this, players,sender)
                    MESSAGE -> MessageCommand(macroName, macroCommand).run(this, players,sender)
                    STRETCH -> StretchCommand(macroName, macroCommand).run(this, players,sender)
                    PLAY_SOUND -> PlaySoundCommand(macroName, macroCommand).run(this, players,sender)
                    else -> {
                        error("unknown macro type : ${macroCommand.type}", sender)
                    }
                }
            } catch (e: Exception) {
                error("macro error $macroName($index): ${e.message}", sender)
            }
        }
        return true
    }

    // endregion
}