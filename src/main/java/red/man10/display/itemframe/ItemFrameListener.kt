package red.man10.display.itemframe

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataType
import red.man10.display.Main

class ItemFrameListener : Listener {
    @EventHandler
    fun hangingBreak(e: HangingBreakByEntityEvent) {
        if (e.isCancelled) return
        getEntityAsMap(e.entity) ?: return

        val remover = e.remover

        if (remover == null) {
            e.isCancelled = true
            return
        }

        if (remover is Player) {
            val hand = remover.inventory.itemInMainHand
            if (isWand(hand)) {
                remover.sendMessage(Main.prefix + "§bProtection forcibly destroyed")
                return
            }
        }

        //if (remover is Player) {
        //    remover.sendMessage(Main.prefix + "§4This item frame is protected")
        // }

        e.isCancelled = true
    }

    @EventHandler
    fun interactEvent(e: PlayerInteractEntityEvent) {
        if (e.isCancelled) return
        getEntityAsMap(e.rightClicked) ?: return

        val hand = e.player.inventory.itemInMainHand
        if (isWand(hand)) {
            e.player.sendMessage(Main.prefix + "§bIgnored protections")
            return
        }

        e.isCancelled = true
        // タッチ可能性がある場合は、メッセージを表示させない
        //  e.player.sendMessage(Main.prefix + "§4This item frame is protected")
    }

    @EventHandler
    fun entityDamageEvent(e: EntityDamageEvent) {
        if (e.isCancelled || e is EntityDamageByEntityEvent) return
        getEntityAsMap(e.entity) ?: return
        e.isCancelled = true
    }

    @EventHandler
    fun entityDamageByEntityEvent(e: EntityDamageByEntityEvent) {
        if (e.isCancelled) return
        getEntityAsMap(e.entity) ?: return
        val remover = e.damager

        if (remover is Player) {
            val hand = remover.inventory.itemInMainHand
            if (isWand(hand)) {
                remover.sendMessage(Main.prefix + "§bIgnored protections")
                return
            }
            // タッチ可能性がある場合は、メッセージを表示させない
//            remover.sendMessage(Main.prefix + "§4This item frame is protected")
        }

        e.isCancelled = true
    }

    @EventHandler
    fun blockBreakEvent(e: BlockBreakEvent) {
        if (e.isCancelled) return
        val entities = e.block.location.subtract(-0.5, -0.5, -0.5).getNearbyEntities(0.7, 0.7, 0.7)
            .filter { it.type == EntityType.ITEM_FRAME || it.type == EntityType.GLOW_ITEM_FRAME }
        if (entities.isEmpty()) return
        for (entity in entities) {
            getEntityAsMap(entity) ?: return
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun entityExplodeEvent(e: EntityExplodeEvent) {
        if (e.isCancelled) return
        val list = e.blockList().filter { isProtectedBlock(it.location) }
        if (list.isEmpty()) return
        list.forEach {
            e.blockList().remove(it)
        }
    }

    @EventHandler
    fun blockExplodeEvent(e: BlockExplodeEvent) {
        if (e.isCancelled) return
        val list = e.blockList().filter { isProtectedBlock(it.location) }
        if (list.isEmpty()) return
        list.forEach {
            e.blockList().remove(it)
        }
    }

    @EventHandler
    fun blockPlaceEvent(e: BlockPlaceEvent) {
        if (e.isCancelled) return

        if (isProtectedBlock(e.block.location.subtract(0.0, 1.0, 0.0))) {
            e.player.sendMessage(Main.prefix + "§4locks cannot be placed near the item frame")
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun playerBucketEmptyEvent(e: PlayerBucketEmptyEvent) {
        if (e.isCancelled) return
        if (e.bucket == Material.MILK_BUCKET) return
        if (isProtectedBlock(e.block.location.subtract(0.0, 1.0, 0.0))) {
            e.player.sendMessage(Main.prefix + "§4Blocks cannot be placed near the item frame")
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun blockFadeEvent(e: BlockFadeEvent) {
        if (isProtectedBlock(e.block.location)) e.isCancelled = true
    }


    private fun getEntityAsMap(entity: Entity): MapView? {
        if (entity !is ItemFrame) return null
        val meta = entity.item.itemMeta as? MapMeta ?: return null
        val mapView = meta.mapView ?: return null
        val mapId = mapView.id
        //  if (Main.displayManager.displays.none { it.mapIds.contains(mapView.id) }) return null
        // displayがprotect=falseの時は保護しない
        for (display in Main.displayManager.displays) {
            if (display.mapIds.contains(mapId)) {
                if (display.protect) {
                    return mapView
                }
            }
        }
        return null
    }

    private fun isProtectedBlock(location: Location): Boolean {
        val entities = location.subtract(-0.5, -0.5, -0.5).getNearbyEntities(1.5, 1.5, 1.5)
            .filter { it.type == EntityType.ITEM_FRAME || it.type == EntityType.GLOW_ITEM_FRAME }
        if (entities.isEmpty()) return false
        for (entity in entities) {
            getEntityAsMap(entity) ?: continue

            val loc = entity.location.toBlockLocation()
            loc.add(0.0, -1.0, 0.0)

            val breakLoc = location.toBlockLocation()

            if (breakLoc == loc) {
                return true
            }
        }
        return false
    }

    private fun isWand(item: ItemStack): Boolean {
        return !item.type.isAir && item.itemMeta.persistentDataContainer.has(
            NamespacedKey(Main.plugin, "Man10DisplayWand"),
            PersistentDataType.INTEGER
        )
    }

    companion object {
        fun getEntityAsMap(entity: Entity): MapView? {
            if (entity !is ItemFrame) return null
            val meta = entity.item.itemMeta as? MapMeta ?: return null
            val mapView = meta.mapView ?: return null
            if (Main.displayManager.displays.none { it.mapIds.contains(mapView.id) }) return null
            return mapView
        }
    }
}