package red.man10.display
import red.man10.extention.getItemFrame
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.*
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import red.man10.display.filter.*
import red.man10.display.macro.MacroEngine
import red.man10.extention.fillCircle
import java.awt.Color
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

class DisplayManager<Entity>(main: JavaPlugin)   : Listener {
    val displays = mutableListOf<Display>()
    private val lastInteractTime = HashMap<UUID, Long>()

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)
    }

    fun deinit(){
        this.stopAllMacro()
        for (display in displays) {
            if(display is StreamDisplay){
                info("stream display ${display.name} deinit start")
                display.deinit()
                info("stream display ${display.name} deinited")
            }
        }
        displays.clear()
        info("displays cleared")
    }

    val names:ArrayList<String>
        get() {
            val nameList = arrayListOf<String>()
            for (display in displays) {
                nameList.add(display.name)
            }
            return nameList
        }
    val parameterKeys:ArrayList<String>
        get() {
            return arrayListOf(
                "fps","interval","dithering","location","protect",
                "fast_dithering","show_status",
                "monochrome","sepia","invert","flip",
                "saturation_level","color_enhancer",
                "keep_aspect_ratio","aspect_ratio_width", "aspect_ratio_height",
                "noise_level","noise",
                "raster_noise","raster_noise_level",
                "vignette","vignette_level",
                "quantize_level","quantize",
                "sobel_level","sobel",
                "cartoon",
                "blur","blur_radius",
                "denoise","denoise_radius",
                "contrast","contrast_level","brightness","brightness_level",
                "sharpen","sharpen_level",
                "scanline","scanline_height",
                "distance",
                "parallel_dithering","parallelism",
                "test_mode")
        }
    fun getDisplay(name: String): Display? {
        displays.find { it.name == name }?.let {
            return it
        }
        return null
    }
    private fun findKey(mapId:Int):String?{
        for (display in displays) {
            if(display.mapIds.contains(mapId)){
                return display.name
            }
        }
        return null
    }
    private fun getDisplay(mapId: Int): Display? {
        val name = findKey(mapId) ?: return null
        return getDisplay(name)
    }

    fun create(player: Player,display: Display) : Boolean{
        if(getDisplay(display.name) != null){
            player.sendMessage(Main.prefix + "§a§l ${display.name} already exists")
            return false
        }
        if(!createMaps(display,player,display.width,display.height)){
            return false
        }
        display.location = player.location
        displays.add(display)
        return true
    }
    fun delete(p: CommandSender, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        display.deinit()
        displays.remove(display)
        return true
    }
    fun getMaps(player: Player, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        return getMaps(display,player)
    }
    fun showList(p: CommandSender): Boolean {
        p.sendMessage(Main.prefix + "§a§l Display List")
        for (display in displays) {
            p.sendMessage("§a§l ${display.name} ${display.className} ${display.width}x${display.height} ${display.locInfo}")
        }
        return true
    }

    fun showStats(sender: CommandSender, name: String): Boolean {
        sender.sendMessage(Main.prefix + "§a§l Display Stats")
        val display = getDisplay(name) ?: return false
        val stats = display.getStatistics()
        for(v in stats){
            sender.sendMessage("§a§l $v")
        }

        return true
    }
    private fun createMaps(display:Display, player: Player, xSize:Int, ySize:Int): Boolean {
        for(y in 0 until ySize){
            for(x in 0 until xSize){
                val mapView = Bukkit.getServer().createMap(player.world)
                mapView.scale = MapView.Scale.CLOSEST
                mapView.isUnlimitedTracking = true

                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = mapView

                val name = "${x+1}-${y+1}"
                mapMeta.displayName(Component.text(name))
                itemStack.itemMeta = mapMeta

                player.world.dropItem(player.location,itemStack)
                display.mapIds.add(mapView.id)
                player.sendMessage("$name created")
            }
        }
        return true
    }

    private fun getMaps(display:Display, player: Player): Boolean {
        val items = getMaps(display)
        for(item in items){
            player.world.dropItem(player.location,item)
        }
        return true
    }

    fun getMaps(display: Display): ArrayList<ItemStack> {
        val items = arrayListOf<ItemStack>()
        for(y in 0 until display.height){
            for(x in 0 until display.width){
                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = Bukkit.getMap(display.mapIds[y * display.width + x])

                val name = "${x+1}-${y+1}"
                mapMeta.displayName(Component.text(name))
                itemStack.itemMeta = mapMeta
                items.add(itemStack)
            }
        }
        return items
    }

    fun save(p: CommandSender): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "displays.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        try{
            for(display in displays){
                display.save(config,display.name)
            }
            config.save(file)
        }catch (e:Exception){
            error(e.message!!,p)
            return false
        }
        return true
    }

    fun load(p: CommandSender? = null ): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "displays.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        deinit()
        for (key in config.getKeys(false)) {
            val className = config.getString("$key.class")
            if(className == StreamDisplay::class.simpleName){
                val display = StreamDisplay(config,key)
                displays.add(display)
                continue
            }
            if(className == ImageDisplay::class.simpleName){
                val display = ImageDisplay(config,key)
                displays.add(display)
                continue
            }
        }
        return true
    }

    fun set(sender: CommandSender, displayName: String, key: String, value: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        val ret = display.set(sender,key,value)
        if(ret)
            save(sender)
            display.clearCache()
        return ret
    }
    fun refresh(sender: CommandSender, displayName: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        display.resetStats()
        display.refreshFlag = true
        return  true
    }

    fun stopAll(sender: CommandSender): Boolean {
        for (display in displays) {
            stopMacro(sender,display.name)
        }
        return true
    }

    fun runMacro(sender: CommandSender, displayName: String,macroName: String? = null): Boolean {
        val display = getDisplay(displayName) ?: return false

        if(macroName == null){
            display.macroEngine.stop()
            display.update()
            return false
        }
        display.runMacro(macroName)
        display.resetStats()
        display.refreshFlag = true
        return  true
    }
    fun stopMacro(sender: CommandSender, displayName: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        display.macroEngine.stop()
        display.reset()
        return true
    }
    fun stopAllMacro(): Boolean {
        for (display in displays) {
            display.macroEngine.stop()
            display.update()
        }
        return true
    }
    fun image(sender: CommandSender, displayName: String,path:String): Boolean {
        val display = getDisplay(displayName) ?: return false
       // display.image(path)
        return true
    }


    fun showMacroList(sender: CommandSender): Boolean {
        val list = MacroEngine.macroList
        sender.sendMessage(Main.prefix + "§a§l Macro List")
        for (macro in list) {
            sender.sendMessage("§a§l $macro")
        }
        return true
    }


    fun reset(sender: CommandSender, displayName: String): Boolean {
        val display = getDisplay(displayName) ?: return false

        // reset to default
        display.dithering = false
        display.fastDithering = false
        display.showStatus = false
        display.monochrome = false
        display.sepia = false
        display.dithering = false
        display.fastDithering = false
        display.showStatus = false
        display.flip = false
        display.invert = false
        display.saturationLevel = 1.0
        display.colorEnhancer = false
        display.keepAspectRatio = false
        display.aspectRatioWidth = 16.0
        display.aspectRatioHeight = 9.0
        display.noise = false
        display.noiseLevel = DEFAULT_NOISE_LEVEL
        display.sobel = false
        display.sobelLevel = DEFAULT_SOBEL_LEVEL
        display.quantize = false
        display.quantizeLevel = DEFAULT_QUANTIZE_LEVEL
        display.cartoon = false
        display.blur = false
        display.blurRadius = DEFAULT_BLUR_RADIUS
        display.denoise = false
        display.denoiseRadius = DEFAULT_DENOISE_RADIUS
        display.contrast = false
        display.contrastLevel = DEFAULT_CONTRAST_LEVEL
        display.scanline = false
        display.scanlineHeight = DEFAULT_SCANLINE_HEIGHT
        display.sharpen = false
        display.sharpenLevel = DEFAULT_SHARPEN_LEVEL
        display.brightness = false
        display.brightnessLevel = DEFAULT_BRIGHTNESS_LEVEL
        display.distance = DEFAULT_DISTANCE
        display.parallelDithering = false
        display.parallelism = DEFAULT_PARALLELISM
        display.rasterNoise = false
        display.rasterNoiseLevel = DEFAULT_RASTER_NOISE_LEVEL
        display.vignette = false
        display.vignetteLevel = DEFAULT_VIGNETTE_LEVEL

        display.testMode = false

        // update&save
        display.refreshFlag = true
        save(sender)
        return true
    }

    fun saveImage(player:Player,name:String): Boolean{
        val display = getDisplay(name) ?: return false
        val fileName = Main.imageManager.createKey(player.name)

        if(!Main.imageManager.save(fileName,display.currentImage!!)){
           player.sendMessage(Main.prefix + "§c§l Failed to save image")
           return false
        }

        return true
    }

    // region event handlers

    @EventHandler
    fun onPlayerToggleSneak(e: PlayerToggleSneakEvent) {
        e.player.sendMessage("スニーク")
        /*
        //      プレイヤーがマップを持っていなければ抜け　
        val player = e.player
        val item = player.inventory.itemInMainHand
        if (item.type != Material.FILLED_MAP) {
            return
        }

         */
    }

    var penRadius = 5.0
    var penColor = Color.RED

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from: Location = event.from
        val to: Location = event.to
        if (from.getYaw() !== to.getYaw() || from.getPitch() !== to.getPitch()) {
            //player.sendMessage("向きが変わった")
        }
    }

    // endregion


    fun onRightButtonClick(event:PlayerInteractEvent){
        val player = event.player
        onButtonClick(event)

    }
    fun onLeftButtonClick(event:PlayerInteractEvent){
        val player = event.player
        onButtonClick(event)

        penRadius = Math.random() * 40 + 5
        val r = Math.random() * 255
        val g = Math.random() * 255
        val b = Math.random() * 255
        val col = Color(r.toInt(), g.toInt(), b.toInt())
        penColor =col

    }

    private fun interactMap(player:Player){
        val distance = 30.0
        val rayTraceResult = player.rayTraceBlocks(distance)

        // 衝突点
        val collisionLocation = rayTraceResult?.hitPosition?.toLocation(player.world)

        // プレイヤーから衝突点へのベクトル
        val rayVector =  player.eyeLocation.toVector().subtract(collisionLocation!!.toVector())

        //額縁との衝突点の計算のための係数
        val multiplier= calculateFrameDiffMultiplier(rayTraceResult.hitBlockFace,rayVector) ?:0.0

        // 額縁との衝突点
        val frameCollisionLocation= collisionLocation.clone().add(rayVector.clone().multiply(multiplier) ?: Vector(0,0,0))

        val face = rayTraceResult.hitBlockFace

        val frame = rayTraceResult.hitBlock?.getItemFrame(face!!) ?: return
        val item = frame.item ?: return
        if(item.type!=Material.FILLED_MAP)
            return
        val mapMeta = item.itemMeta as MapMeta
        val mapView = mapMeta.mapView ?: return
        val mapId = mapView.id

        val result = calculatePixelCoordinate(face, rayVector,collisionLocation)


        onMapClick(player, mapId,result.first.toInt(), result.second.toInt())

    }


    fun onButtonClick(event:PlayerInteractEvent) {
        interactMap(event.player)


    }

    private fun calculatePixelCoordinate(face: BlockFace?, rayVector: Vector?,collisionLocation:Location?):Pair<Double,Double>{
        if(face==null||rayVector==null||collisionLocation==null)
            return Pair(0.0,0.0)

        val t= if(face== EAST ||face== WEST){
            rayVector.x
        }
        else if(face== SOUTH ||face== NORTH){
            rayVector.z
        }
        else if(face== UP ||face== DOWN){
            rayVector.y
        }
        else{
            1.0
        }
        val frameCollisionLocation=collisionLocation.clone().add(rayVector.clone().multiply(abs(1.0/16.0/t)))

        val height= floor(if(face== UP ||face== DOWN){
            frameCollisionLocation.x.mod(1.0)
        }
        else{
            1-frameCollisionLocation.y.mod(1.0)
        }*128.0)

        val width= floor(
            when (face) {
                SOUTH -> frameCollisionLocation.x.mod(1.0)
                NORTH -> 1-frameCollisionLocation.x.mod(1.0)
                EAST -> 1-frameCollisionLocation.z.mod(1.0)
                WEST -> frameCollisionLocation.z.mod(1.0)
                else -> 0.0
            }*128.0
        )
        return Pair(width,height)
    }

    // 額縁との衝突点の計算のための係数
    private fun calculateFrameDiffMultiplier(face: BlockFace?, rayVector: Vector?):Double{
        if(face==null||rayVector==null)return 0.0
        val t=if(face== EAST ||face== WEST){
            rayVector.x
        }
        else if(face== SOUTH ||face== NORTH){
            rayVector.z
        }
        else if(face== UP ||face== DOWN){
            rayVector.y
        }
        else{
            1.0
        }
        return abs(1.0/16.0/t)
    }


    fun onMapClick(player:Player,mapId:Int,x:Int,y:Int):Boolean{
       // player.sendMessage("§a§l Clicked Map $mapId $x $y")
        val display = getDisplay(mapId) ?: return false

        val xy = display.getImageXY(mapId,x, y)
        val imageX = xy.first
        val imageY =  xy.second

        var distance = display.location?.distance(player.location)
        var r = 3.0
        if(distance!=null){
            r = distance/10
        }
        if(r < 5){
            r = 5.0
        }

        display.update(display.currentImage?.fillCircle(imageX , imageY ,penRadius.toInt() ,penColor))
   //     display.refresh()
        return true
    }
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action: Action = event.action

        // プレイヤーが右クリック
        if (action === Action.RIGHT_CLICK_AIR || action === Action.RIGHT_CLICK_BLOCK) {
            onRightButtonClick(event)
            // プレイヤーが左クリック
        } else if (action === Action.LEFT_CLICK_AIR || action === Action.LEFT_CLICK_BLOCK) {
            onLeftButtonClick(event)
        }
    }


    @EventHandler
    fun onPlayerInteractEntityEvent(e: PlayerInteractEntityEvent): Boolean {
        interactMap(e.player)
        return true
    }

    fun showInfo(sender:CommandSender,name:String): Boolean {
        val display = getDisplay(name) ?: return false
        return display.showInfo(sender)
    }


}