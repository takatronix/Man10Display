package red.man10.display

import getItemStackInFrame
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import placeMap
import red.man10.display.itemframe.ItemFrameCoordinate
import red.man10.display.macro.MacroEngine
import red.man10.extention.*
import removeFrame
import java.awt.Color
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

var PLAYER_DATA_THREAD_INTERVAL = 10L
var RIGHT_BUTTON_UP_DETECTION_INTERVAL = 260L

class DisplayPlayerData {
    var lastLocation: Location? = null
    var rightButtonPressed = false
    var isSneaking = false

    // var leftClick
    var rightClickDown = false
    var lastRightClickTime: Long = 0
    var lastLeftClickTime: Long = 0
    var lastFocusX: Int = -1
    var lastFocusY: Int = -1


    var penWidth: Int = 1
    var penColor: Color = Color.RED
    var hasPen: Boolean = false
    var focusingDisplay: Display? = null
    var focusingMapId: Int = -1
    var focusingImageX: Int = -1
    var focusingImageY: Int = -1
    var lastFocusingImageX: Int = -1
    var lastFocusingImageY: Int = -1

}

class DisplayManager : Listener {
    val displays = mutableListOf<Display>()
    private val playerData = ConcurrentHashMap<UUID, DisplayPlayerData>()
    private var playerDataThread: Thread? = null
    private var isCopyDisabled = true

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)

        this.load(null)

        playerDataThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    playerDataTask()
                    Thread.sleep(PLAYER_DATA_THREAD_INTERVAL)
                } catch (e: InterruptedException) {
                    error(e.localizedMessage)
                    //Thread.currentThread().interrupt()
                }
            }
        }.apply(Thread::start)
    }

    fun deinit() {
        playerDataThread?.interrupt()
        this.stopAllMacro()
        for (display in displays) {
            info("stream display ${display.name} deinit start")
            display.deinit()
            info("stream display ${display.name} deinited")
        }
        displays.clear()
        info("displays cleared")
    }

    val displayNames: ArrayList<String>
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
            var macroName = display.macroName ?: ""
            var autoRun = if (display.autoRun) "§a§l[AutoRun]" else ""


            if (display.macroEngine.isRunning()) {
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

                display.mapIds.add(mapView.id)
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

    private fun getMaps(display: Display): ArrayList<ItemStack> {
        val items = arrayListOf<ItemStack>()
        for (y in 0 until display.height) {
            for (x in 0 until display.width) {
                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.mapView = Bukkit.getMap(display.mapIds[y * display.width + x])

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
            if (className == Display::class.simpleName) {
                val display = Display(config, key)
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
        if (!canInteract(player)) {
            return
        }
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

        //player.sendMessage("§a§l Clicked Map $mapId $result")

        if (result.first < 0 || result.first > 127)
            return
        if (result.second < 0 || result.second > 127)
            return

        val display = getDisplay(mapId) ?: return
        val xy = display.getImageXY(mapId, result.first.toInt(), result.second.toInt())
        val imageX = xy.first
        val imageY = xy.second

        playerData[player.uniqueId]?.focusingDisplay = display
        playerData[player.uniqueId]?.focusingMapId = mapId
        playerData[player.uniqueId]?.lastFocusingImageX = playerData[player.uniqueId]?.focusingImageX ?: -1
        playerData[player.uniqueId]?.lastFocusingImageY = playerData[player.uniqueId]?.focusingImageY ?: -1
        playerData[player.uniqueId]?.focusingImageX = imageX
        playerData[player.uniqueId]?.focusingImageY = imageY
    }

    fun showInfo(sender: CommandSender, name: String): Boolean {
        val display = getDisplay(name) ?: return false
        return display.showInfo(sender)
    }
    // region event handlers

    // ログインイベント
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        info("Player Join ${e.player.name}")
        val player = e.player
        //プレイヤーデータを初期化
        playerData[player.uniqueId] = DisplayPlayerData()


        // 3秒後にディスプレイを表示
        val delay = 3 * 20L
        Bukkit.getScheduler().runTaskLater(Main.plugin, Runnable {
            // プレイヤーにディスプレイ表示
            displays.forEach { it.show(player) }
        }, delay)

    }

    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        if (isCopyDisabled) {
            val matrix = event.inventory.matrix
            for (item in matrix) {
                if (item != null && item.type == Material.FILLED_MAP) {
                    event.isCancelled = true
                    event.whoClicked.sendMessage(Main.prefix + "§c§lCopying of maps is prohibited.")
                    return
                }
            }
        }
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

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from: Location = event.from
        val to: Location = event.to
        if (from.yaw !== to.yaw || from.pitch !== to.pitch) {
            //player.sendMessage("向きが変わった")
            //    if(!playerData[player.uniqueId]?.rightButtonPressed!!)
            //         return
            //      onRightButtonEvent(player)
        }

    }

    @EventHandler
    fun onMapInitialize(event: MapInitializeEvent) {
        val mapView: MapView = event.map
        //info("onMapInitialize ${mapView.id}")

        var display = getDisplay(mapView.id) ?: return


        for (renderer in mapView.renderers) {
            mapView.removeRenderer(renderer)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action: Action = event.action
        //  info("onPlayerInteract ${action.name}",player)
        // プレイヤーが右クリック
        if (action === Action.RIGHT_CLICK_AIR || action === Action.RIGHT_CLICK_BLOCK) {
            onRightButtonEvent(player)
            // プレイヤーが左クリック
        } else if (action === Action.LEFT_CLICK_AIR || action === Action.LEFT_CLICK_BLOCK) {
            onLeftButtonEvent(player)
        }
    }


    // 近くで右クリックしたとき
    @EventHandler
    fun onPlayerInteractEntityEvent(e: PlayerInteractEntityEvent) {
        // info("onPlayerInteractEntityEvent ",e.player)
        val player = e.player
        onRightButtonEvent(player)
    }

    @EventHandler
    fun onInventoryOpen(e: InventoryOpenEvent) {
        //info("onInventoryOpen")
    }

    @EventHandler
    fun onItemHeld(e: PlayerItemHeldEvent) {
        //info("onItemHeld")
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        //info("onInventoryClick")
    }

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        //info("onInventoryDrag")
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        //info("onInventoryClose")
    }

    @EventHandler
    fun onInventoryMoveItem(e: InventoryMoveItemEvent) {
        //info("onInventoryMoveItem")
    }

    @EventHandler
    fun onInventoryPickupItem(e: InventoryPickupItemEvent) {
        //info("onInventoryPickupItem")
    }

    @EventHandler
    fun onInventory(e: InventoryEvent) {
        //info("onInventory")
    }
    // endregion

    // region Mouse Event
    fun onButtonClick(player: Player) {
        interactMap(player)
    }

    // 右クリックイベント
    fun onRightButtonEvent(player: Player) {
        onButtonClick(player)

        val lastClick = this.playerData[player.uniqueId]?.lastRightClickTime ?: 0
        val now = System.currentTimeMillis()

        this.playerData[player.uniqueId]?.lastRightClickTime = now

        if (this.playerData[player.uniqueId]?.rightButtonPressed == false) {
            this.playerData[player.uniqueId]?.rightButtonPressed = true
            onRightButtonDown(player)
        } else {
            onRightButtonMove(player)
        }
    }


    fun onRightButtonUp(player: Player) {
        //info("onRightButtonUp", player)
        if (playerData[player.uniqueId]?.hasPen == false)
            return

        drawLine(player)
        // 最終ポイントたをクリア
        this.playerData[player.uniqueId]?.lastFocusingImageX = -1
        this.playerData[player.uniqueId]?.lastFocusingImageY = -1

    }

    fun drawLine(player: Player) {
        if (playerData[player.uniqueId]?.hasPen == false)
            return

        var penWidth = playerData[player.uniqueId]?.penWidth ?: return
        var penColor = playerData[player.uniqueId]?.penColor ?: return
        val display = playerData[player.uniqueId]?.focusingDisplay ?: return

        val x = playerData[player.uniqueId]?.lastFocusingImageX ?: return
        val y = playerData[player.uniqueId]?.lastFocusingImageY ?: return
        val x2 = playerData[player.uniqueId]?.focusingImageX ?: return
        val y2 = playerData[player.uniqueId]?.focusingImageY ?: return


        // player.sendMessage("drawLine $x $y $x2 $y2")
        val rect = display.currentImage?.drawLine(x, y, x2, y2, penWidth, penColor)
        display.update(rect!!)

    }

    fun onRightButtonDown(player: Player) {
        // info("onRightButtonDown", player)
        if (playerData[player.uniqueId]?.hasPen == false)
            return
        playerData[player.uniqueId]?.lastFocusingImageX = playerData[player.uniqueId]?.focusingImageX!!
        playerData[player.uniqueId]?.lastFocusingImageY = playerData[player.uniqueId]?.focusingImageY!!

        var penWidth = playerData[player.uniqueId]?.penWidth
        var penColor = playerData[player.uniqueId]?.penColor
        if (penWidth == 0) {
            val display = playerData[player.uniqueId]?.focusingDisplay ?: return
            display.update(display.currentImage?.fill(penColor!!))
            return
        }


    }

    fun onRightButtonMove(player: Player) {
        // info("onRightButtonMove", player)
        if (playerData[player.uniqueId]?.hasPen == false)
            return
        if (playerData[player.uniqueId]?.lastFocusingImageX == -1)
            return
        if (playerData[player.uniqueId]?.lastFocusingImageY == -1)
            return

        drawLine(player)
    }

    //  左クリックイベント
    fun onLeftButtonEvent(player: Player) {
        onButtonClick(player)

    }

    // endregion
    private fun canInteract(player: Player): Boolean {

        // 手に持っているアイテムのPersistentDataを取得
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta
        val pd = meta?.persistentDataContainer ?: return false
        meta.persistentDataContainer
        interactItem(player,pd)

        // ペンを持っているか
        // PersistentDataの中身を取得
        val type = pd.get(Main.plugin, "man10display.type", PersistentDataType.STRING)
        val width = pd.get(Main.plugin, "man10display.pen.width", PersistentDataType.INTEGER)
        val color = pd.get(Main.plugin, "man10display.pen.color", PersistentDataType.STRING)
        if (type != "pen") {
            playerData[player.uniqueId]?.hasPen = false

            return false
        }
        playerData[player.uniqueId]?.penWidth = width!!.toInt()
        playerData[player.uniqueId]?.penColor = Color.decode(color!!)
        playerData[player.uniqueId]?.hasPen = true
        return true
    }
    private fun interactItem(player:Player,pd: PersistentDataContainer):Boolean {
        var command = pd.get(Main.plugin, "man10display.ticket.command", PersistentDataType.STRING)
        var op_command = pd.get(Main.plugin, "man10display.ticket.op_command", PersistentDataType.STRING)

        // %player%をプレイヤー名に置き換え
        command = command?.replace("%player%",player.name)
        op_command = op_command?.replace("%player%",player.name)
        // #はスペースにおきかえ
        command = command?.replace("#"," ")
        op_command = op_command?.replace("#"," ")


        if(command != null){
            info("command $command",player)
            Bukkit.getServer().dispatchCommand(player,command)
        }
        if(op_command != null){
            info("op_command $op_command",player)
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),op_command)
        }
        return true
    }


    private fun playerDataTask() {
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid = player.uniqueId
            if (!playerData.containsKey(uuid)) {
                playerData[uuid] = DisplayPlayerData()
            }

            val data = playerData[uuid]!!

            // 右ボタンアップを検出
            val delta = System.currentTimeMillis() - data.lastRightClickTime
            if (delta >= RIGHT_BUTTON_UP_DETECTION_INTERVAL) {
                if (data.rightButtonPressed) {
                    //info("$delta ms", player)
                    onRightButtonUp(player)
                    data.rightButtonPressed = false
                }
            }
        }

    }

    private fun inventoryCheckTask() {
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

    fun setupDisplay(display: Display, player: Player, isGrowing: Boolean = false): Boolean {
        val distance = 32.0
        val rayTraceResult = player.rayTraceBlocks(distance)
        val hitPosition = rayTraceResult?.hitPosition
        if (hitPosition == null) {
            error("Could not find a place to install", player)
            return false
        }

        //  視線の衝突点
        val collisionLocation = hitPosition.toLocation(player.world)
        // 衝突したブロックの面
        val face = rayTraceResult.hitBlockFace ?: return false

        val width = display.width
        val height = display.height

        // face面の方向に向かって(width/height)分だけ額縁を設置する
        placeMaps(player, collisionLocation, face, width, height, display.mapIds, isGrowing)

        return true
    }


    fun placeMaps(
        player: Player,
        startLocation: Location,
        face: BlockFace,
        width: Int,
        height: Int,
        mapIds: List<Int>,
        isGlowing: Boolean = false
    ) {
        placeMapsNormal(player, startLocation, face, width, height, mapIds, isGlowing)
    }

    fun placeMapsNormal(
        player: Player,
        startLocation: Location,
        face: BlockFace,
        width: Int,
        height: Int,
        mapIds: List<Int>,
        isGlowing: Boolean = false,
        deleteOnly: Boolean = false
    ) {
        val rightDirection = when (face) {
            BlockFace.NORTH -> BlockFace.WEST
            BlockFace.WEST -> BlockFace.SOUTH
            BlockFace.SOUTH -> BlockFace.EAST
            BlockFace.EAST -> BlockFace.NORTH
            else -> throw IllegalArgumentException("Invalid face: $face")
        }
        val downDirection = BlockFace.DOWN

        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                //info("x: $x, y: $y", player)
                val location = startLocation.clone()
                    .add(
                        rightDirection.modX * x.toDouble(),
                        downDirection.modY * y.toDouble(),
                        rightDirection.modZ * x.toDouble()
                    )
                    .add(face.modX.toDouble(), face.modY.toDouble(), face.modZ.toDouble())

                if (face == BlockFace.EAST || face == BlockFace.SOUTH) {
                    location.subtract(face.modX.toDouble(), face.modY.toDouble(), face.modZ.toDouble())
                }

                if (!deleteOnly) {
                    val behindLocation =
                        location.clone().subtract(face.modX.toDouble(), face.modY.toDouble(), face.modZ.toDouble())
                    if (behindLocation.block.type == Material.AIR) {
                        behindLocation.block.type = Material.SEA_LANTERN
                    }
                    location.placeMap(face, mapIds[index], isGlowing)
                    index++
                } else {
                    if (!location.removeFrame(face)) {
                        error("Could not delete frame", player)
                    }
                }
            }
        }
    }

    fun removeDisplay(player: Player): Boolean {
        val distance = 32.0
        val rayTraceResult = player.rayTraceBlocks(distance)
        val hitPosition = rayTraceResult?.hitPosition
        if (hitPosition == null) {
            error("Could not find a block", player)
            return false
        }

        //  視線の衝突点
        val collisionLocation = hitPosition.toLocation(player.world)
        // 衝突したブロックの面
        val face = rayTraceResult.hitBlockFace ?: return false

        val item = collisionLocation.getItemStackInFrame(face)
        if (item == null) {
            error("Could not find a display", player)
            return false
        }
        val mapId = item.getMapId()
        if (mapId == null) {
            error("Could not find a display", player)
            return false
        }

        val display = this.getDisplay(mapId)
        if (display == null) {
            error("Could not find a display", player)
            return false
        }
        info("display found: ${display.name} ${display.width} ${display.height}", player)
        this.placeMapsNormal(player, collisionLocation, face, display.width, display.height, listOf(), false, true)

        return true
    }

}