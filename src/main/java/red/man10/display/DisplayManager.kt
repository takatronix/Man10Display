package red.man10.display

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.itemframe.ItemFrameCoordinate
import red.man10.display.macro.MacroEngine
import red.man10.extention.fillCircle
import red.man10.extention.getItemFrame
import red.man10.extention.sendClickableMessage
import java.awt.Color
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap


var INVENTORY_CHECK_INTERVAL = (3 * 20).toLong()

class PlayerData {
    var lastLocation: Location? = null
    var rightButtonPressed = false
    var isSneaking = false

    var lastRightClickTime : Long = 0
}

class DisplayManager(main: JavaPlugin) : Listener {
    val displays = mutableListOf<Display>()
    private val playerData = ConcurrentHashMap<UUID, PlayerData>()

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)

        // 3秒毎のタスクを起動
        Bukkit.getScheduler().runTaskTimer(main, Runnable {
        //    inventoryCheckTask()
        }, 0, INVENTORY_CHECK_INTERVAL)
    }

    fun deinit() {
        this.stopAllMacro()
        for (display in displays) {
            if (display is StreamDisplay) {
                info("stream display ${display.name} deinit start")
                display.deinit()
                info("stream display ${display.name} deinited")
            }
        }
        displays.clear()
        info("displays cleared")
    }

    val names: ArrayList<String>
        get() {
            val nameList = arrayListOf<String>()
            for (display in displays) {
                nameList.add(display.name)
            }
            return nameList
        }

    fun getDisplay(name: String): Display? {
        displays.find { it.name == name }?.let {
            return it
        }
        return null
    }

    private fun findKey(mapId: Int): String? {
        for (display in displays) {
            if (display.mapIds.contains(mapId)) {
                return display.name
            }
        }
        return null
    }

    private fun getDisplay(mapId: Int): Display? {
        val name = findKey(mapId) ?: return null
        return getDisplay(name)
    }

    fun create(player: Player, display: Display): Boolean {
        if (getDisplay(display.name) != null) {
            player.sendMessage(Main.prefix + "§a§l ${display.name} already exists")
            return false
        }
        if (!createMaps(display, player, display.width, display.height)) {
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
        return getMaps(display, player)
    }

    fun showList(p: CommandSender): Boolean {
        p.sendMessage(Main.prefix + "§§l Display List")
        for (display in displays) {

            var macroInfo = ""
            var color = "§7"
            if(display.macroEngine.isRunning()){
                color = "§a§l"
                val currentMacro = display.macroEngine.macroName
                macroInfo = "§c§l[Running] {§b§n${currentMacro}:/md stats ${display.name}}"
            }

            val locInfo = "{§b§n${display.locInfo}:/md tp ${display.name}}"

            p.sendClickableMessage("${color}${display.name} ${display.width}x${display.height} ${locInfo} ${macroInfo}")
        }
        return true
    }

    fun showStats(sender: CommandSender, name: String): Boolean {
        sender.sendMessage(Main.prefix + "§a§l Display Stats")
        val display = getDisplay(name) ?: return false
        val stats = display.getStatistics()
        for (v in stats) {
            sender.sendMessage("§a§l $v")
        }

        return true
    }

    private fun createMaps(display: Display, player: Player, xSize: Int, ySize: Int): Boolean {
        for (y in 0 until ySize) {
            for (x in 0 until xSize) {
                val mapView = Bukkit.getServer().createMap(player.world)
                mapView.scale = MapView.Scale.CLOSEST
                mapView.isUnlimitedTracking = true

                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = mapView

                val name = "${x + 1}-${y + 1}"
                mapMeta.displayName(Component.text(name))
                itemStack.itemMeta = mapMeta

                player.world.dropItem(player.location, itemStack)
                display.mapIds.add(mapView.id)
                player.sendMessage("$name created")
            }
        }
        return true
    }

    private fun getMaps(display: Display, player: Player): Boolean {
        val items = getMaps(display)
        for (item in items) {
            player.world.dropItem(player.location, item)
        }
        return true
    }

    fun getMaps(display: Display): ArrayList<ItemStack> {
        val items = arrayListOf<ItemStack>()
        for (y in 0 until display.height) {
            for (x in 0 until display.width) {
                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = Bukkit.getMap(display.mapIds[y * display.width + x])

                val name = "${x + 1}-${y + 1}"
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
        try {
            for (display in displays) {
                display.save(config, display.name)
            }
            config.save(file)
        } catch (e: Exception) {
            error(e.message!!, p)
            return false
        }
        return true
    }

    fun load(p: CommandSender? = null): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "displays.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        deinit()
        for (key in config.getKeys(false)) {
            val className = config.getString("$key.class")
            if (className == StreamDisplay::class.simpleName) {
                val display = StreamDisplay(config, key)
                displays.add(display)
                continue
            }
            if (className == ImageDisplay::class.simpleName) {
                val display = ImageDisplay(config, key)
                displays.add(display)
                continue
            }
        }
        return true
    }

    fun set(sender: CommandSender, displayName: String, key: String, value: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        val ret = display.set(sender, key, value)
        if (ret)
            save(sender)
        display.clearCache()
        return ret
    }

    fun refresh(sender: CommandSender, displayName: String): Boolean {
        val display = getDisplay(displayName) ?: return false
        display.resetStats()
        display.refreshFlag = true
        return true
    }

    fun stopAll(sender: CommandSender): Boolean {
        for (display in displays) {
            stopMacro(sender, display.name)
        }
        return true
    }

    fun runMacro(sender: CommandSender, displayName: String, macroName: String? = null): Boolean {
        val display = getDisplay(displayName) ?: return false

        if (macroName == null) {
            display.macroEngine.stop()
            display.update()
            return false
        }
        display.runMacro(macroName)
        display.resetStats()
        display.refreshFlag = true
        display.macroName = macroName
        save(sender)
        return true
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

    fun image(sender: CommandSender, displayName: String, path: String): Boolean {
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
        display.resetParams(sender)
        save(sender)
        return true
    }

    fun saveImage(player: Player, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        val fileName = Main.imageManager.createKey(player.name)

        if (!Main.imageManager.save(fileName, display.currentImage!!)) {
            player.sendMessage(Main.prefix + "§c§l Failed to save image")
            return false
        }

        return true
    }

    private fun interactMap(player: Player) {
        val distance = 32.0
        val rayTraceResult = player.rayTraceBlocks(distance)
        val hitPosition = rayTraceResult?.hitPosition ?: return

        //  視線の衝突点
        val collisionLocation = hitPosition.toLocation(player.world)
        // プレイヤーから衝突点へのベクトル
        val rayVector = player.eyeLocation.toVector().subtract(collisionLocation.toVector())
        // 額縁との衝突点の計算のための係数
        val multiplier = ItemFrameCoordinate.calculateFrameDiffMultiplier(rayTraceResult.hitBlockFace, rayVector)
        // 額縁との衝突点
        val frameCollisionLocation =
            collisionLocation.clone().add(rayVector.clone().multiply(multiplier))
        // 衝突したブロックの面
        val face = rayTraceResult.hitBlockFace
        //　対象の額縁があるかどうか
        val frame = rayTraceResult.hitBlock?.getItemFrame(face!!) ?: return
        // 額縁に入っているアイテム
        val item = frame.item

        if (item.type != Material.FILLED_MAP)
            return

        // mapIdを取得
        val mapMeta = item.itemMeta as MapMeta
        val mapView = mapMeta.mapView ?: return
        val mapId = mapView.id

        // result.first = x座標 result.second = y座標
        val result = ItemFrameCoordinate.calculatePixelCoordinate(face, rayVector, collisionLocation)

        player.sendMessage("§a§l Clicked Map $mapId $result")


        if(result.first < 0 || result.first > 127)
            return
        if(result.second < 0 || result.second > 127)
            return

        onMapClick(player, mapId, result.first.toInt(), result.second.toInt())
    }


    fun onButtonClick(event: PlayerInteractEvent) {
        interactMap(event.player)
    }


    private fun onMapClick(player: Player, mapId: Int, x: Int, y: Int): Boolean {
        // player.sendMessage("§a§l Clicked Map $mapId $x $y")
        val display = getDisplay(mapId) ?: return false

        val xy = display.getImageXY(mapId, x, y)
        val imageX = xy.first
        val imageY = xy.second

        val distance = display.location?.distance(player.location)
        var r = 3.0
        if (distance != null) {
            r = distance / 10
        }
        if (r < 5) {
            r = 5.0
        }

        display.update(display.currentImage?.fillCircle(imageX, imageY, penRadius.toInt(), penColor))
        //     display.refresh()
        return true
    }

    fun showInfo(sender: CommandSender, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        return display.showInfo(sender)
    }
    // region event handlers

    // ログインイベント
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player
        //プレイヤーデータを初期化
        playerData[player.uniqueId] = PlayerData()
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val player = e.player
        // プレイヤーデータを削除
        playerData.remove(player.uniqueId)
    }

    @EventHandler
    fun onPlayerToggleSneak(e: PlayerToggleSneakEvent) {
        playerData[e.player.uniqueId]?.isSneaking = e.isSneaking
    }

    var penRadius = 5.0
    var penColor = Color.RED

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from: Location = event.from
        val to: Location = event.to
        if (from.yaw !== to.yaw || from.pitch !== to.pitch) {
            //player.sendMessage("向きが変わった")
        }
    }
    @EventHandler
    fun onMapInitialize(event: MapInitializeEvent) {
        val mapView: MapView = event.map
        info("onMapInitialize ${mapView.id}")
        for (renderer in mapView.renderers) {
            mapView.removeRenderer(renderer)
        }
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

    @EventHandler
    fun onInventoryOpen(e: InventoryOpenEvent){
        info("onInventoryOpen")
    }
    @EventHandler
    fun onItemHeld(e: PlayerItemHeldEvent){
        info("onItemHeld")
    }
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        info("onInventoryClick")
    }
    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        info("onInventoryDrag")
    }
    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
  //      info("onInventoryClose",e.player)
    }
    @EventHandler
    fun onInventoryMoveItem(e: InventoryMoveItemEvent) {
        info("onInventoryMoveItem")
    }
    @EventHandler
    fun onInventoryPickupItem(e: InventoryPickupItemEvent) {
//        info("onInventoryPickupItem",e.inventory.viewers.)
    }
    @EventHandler
    fun onInventory(e: InventoryEvent) {
       // info("onInventory",e.view.player)
    }


    // endregion

    fun onRightButtonClick(event: PlayerInteractEvent) {
        val player = event.player
        onButtonClick(event)
    }

    fun onLeftButtonClick(event: PlayerInteractEvent) {
        val player = event.player
        onButtonClick(event)

        penRadius = Math.random() * 40 + 5
        val r = Math.random() * 255
        val g = Math.random() * 255
        val b = Math.random() * 255
        val col = Color(r.toInt(), g.toInt(), b.toInt())
        penColor = col

    }

    private fun inventoryCheckTask(){
        val ip = Bukkit.getServer().ip
        val port = Bukkit.getServer().port
        val serverName = Bukkit.getServer().motd
        var key = "$ip:$port:$serverName"
        // オンラインプレイヤーのinventoryをチェック
        for (player in Bukkit.getOnlinePlayers()) {
            val inventory = player.inventory
            val itemStack = inventory.itemInMainHand
            val itemMeta = itemStack.itemMeta
            val displayName = itemMeta.displayName

        }

    }


}