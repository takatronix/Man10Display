package red.man10.display

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import red.man10.extention.get
import red.man10.extention.getMapId
import red.man10.extention.setMapId
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AppPlayerData {
    var app : App? = null

    var appThread: Thread? = null

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

    var focusingMapId: Int = -1
    var focusingImageX: Int = -1
    var focusingImageY: Int = -1
    var lastFocusingImageX: Int = -1
    var lastFocusingImageY: Int = -1


    fun stop() {
        app?.stop()
        app = null
    }
}
class AppManager(var plugin: JavaPlugin, var appMapId: Int) : Listener {

    private val playerData = ConcurrentHashMap<UUID, AppPlayerData>()
    private var playerDataThread: Thread? = null



    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)


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

    }
    private fun playerDataTask() {
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid = player.uniqueId
            if (!playerData.containsKey(uuid)) {
                playerData[uuid] = AppPlayerData()
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
    // region event handlers

    // ログインイベント
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
      //  info("Player Join ${e.player.name}")
        val player = e.player
        //プレイヤーデータを初期化
        playerData[player.uniqueId] = AppPlayerData()
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val player = e.player
   //     info("Player Quit ${player.name}")
        val data = playerData[player.uniqueId] ?: return
        data.stop()
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

        if(mapView.id != appMapId){
            return
        }
        info("[App]onMapInitialize ${mapView.id}")
        for (renderer in mapView.renderers) {
            mapView.removeRenderer(renderer)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action: Action = event.action
        //info("onPlayerInteract ${action.name}",player)
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

        // インタラクトしたエンティティがItemFrame（額縁）であるかチェック
        if (e.rightClicked is ItemFrame) {
            val itemFrame = e.rightClicked as ItemFrame
            val item = player.inventory.itemInMainHand
            if(item.type != Material.FILLED_MAP){
                return
            }
            val mapId = item.getMapId() ?: return
            if (mapId == appMapId) {
                e.player.sendMessage("§2§lThis item cannot be placed in the item frame.")
                e.isCancelled = true
            }
        }

        onRightButtonEvent(player)
    }

    @EventHandler
    fun onInventoryOpen(e: InventoryOpenEvent) {
        info("onInventoryOpen",e.player)
    }

    fun StartMapItemTask(player: Player, item: ItemStack) {

        // 地図以外は無視
        if (item.type != Material.FILLED_MAP) {
            return
        }
        var mapId = item.getMapId() ?: return

        val data = playerData[player.uniqueId] ?: return
        data.stop()

        val meta = item.itemMeta
        val pd = meta?.persistentDataContainer ?: return
        val key = pd.get(Main.plugin, "man10display.app.key", PersistentDataType.STRING) ?: return

        if(appMapId != mapId){
            item.setMapId(appMapId)
            info("map id changed $mapId to $appMapId by ${player.name}")
            mapId = appMapId
        }

        val image = pd.get(Main.plugin, "man10display.app.image", PersistentDataType.STRING)
        if(image != null){
            info("image $image")
            data.app = App(mapId, player, "image")
            data.app!!.startImageTask(image,player)
        }

        val macro = pd.get(Main.plugin, "man10display.app.macro", PersistentDataType.STRING)
        if (macro != null) {
            info("macro $macro")
            data.app = App(mapId, player, "macro")
            data.app!!.runMacro(macro)
        }

    }

    @EventHandler
    fun onItemHeld(e: PlayerItemHeldEvent) {
        val item = e.player.inventory.getItem(e.newSlot) ?: return
        StartMapItemTask(e.player, item)
    }
    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        // エンティティがプレイヤーであるかチェック
        if (event.entity is Player) {
            val player = event.entity as Player
            val itemEntity = event.item // 拾われたアイテムエンティティ
            // アイテムエンティティからItemStackを取得
            val itemStack: ItemStack = itemEntity.itemStack
            StartMapItemTask(player, itemStack)

        }
    }
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        //info("onInventoryClick",e.whoClicked)
    }

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
       // info("onInventoryDrag",e.whoClicked)
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        //info("onInventoryClose",e.player)
    }

    @EventHandler
    fun onInventoryMoveItem(e: InventoryMoveItemEvent) {
       // info("onInventoryMoveItem")
    }



    @EventHandler
    fun onInventory(e: InventoryEvent) {
    //    info("onInventory",e.view.player)
    }
    // endregion

    // region Mouse Event
    fun onButtonClick(player: Player) {
     //   interactMap(player)
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
    //    info("onRightButtonUp", player)
    //    if (playerData[player.uniqueId]?.hasPen == false)
    //        return
    }



    fun onRightButtonDown(player: Player) {
 //       info("onRightButtonDown", player)
 //       if (playerData[player.uniqueId]?.hasPen == false)
 //           return

    }

    fun onRightButtonMove(player: Player) {

    }

    //  左クリックイベント
    fun onLeftButtonEvent(player: Player) {
        onButtonClick(player)

    }

    // endregion
}

