package red.man10.display.itemframe

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
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
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataType
import red.man10.display.Main

class ItemFrameListener: Listener {
    @EventHandler
    fun hangingBreak(e: HangingBreakByEntityEvent){
        if (e.isCancelled)return
        getEntityAsMap(e.entity)?:return

        val remover = e.remover

        if (remover == null){
            e.isCancelled = true
            return
        }



        if (remover is Player){
            val hand = remover.inventory.itemInMainHand
            if (isStaff(hand)){
                remover.sendMessage(Main.prefix + "§b保護を強制的に破壊しました")
                return
            }
        }

        if (remover is Player){
            remover.sendMessage(Main.prefix + "§4この額縁は保護されています")
        }

        e.isCancelled = true
    }

    @EventHandler
    fun interactEvent(e: PlayerInteractEntityEvent){
        if (e.isCancelled)return
        getEntityAsMap(e.rightClicked)?:return

        val hand = e.player.inventory.itemInMainHand
        if (isStaff(hand)){
            e.player.sendMessage(Main.prefix + "§b保護を無視しました")
            return
        }

        e.isCancelled = true
        e.player.sendMessage(Main.prefix + "§4この額縁は保護されています")
    }

    @EventHandler
    fun entityDamageEvent(e: EntityDamageEvent){
        if (e.isCancelled)return
        getEntityAsMap(e.entity)?:return
        e.isCancelled = true
    }

    @EventHandler
    fun entityDamageByEntityEvent(e: EntityDamageByEntityEvent){
        if (e.isCancelled)return
        getEntityAsMap(e.entity)?:return
        val remover = e.damager

        if (remover is Player){
            val hand = remover.inventory.itemInMainHand
            if (isStaff(hand)){
                remover.sendMessage(Main.prefix + "§b保護を無視しました")
                return
            }
            remover.sendMessage(Main.prefix + "§4この額縁は保護されています")
        }

        e.isCancelled = true
    }

    @EventHandler
    fun blockBreakEvent(e: BlockBreakEvent){
        if (e.isCancelled)return
        val entities = e.block.location.subtract(-0.5,-0.5,-0.5).getNearbyEntities(1.5,1.5,1.5).filter { it.type == EntityType.ITEM_FRAME || it.type == EntityType.GLOW_ITEM_FRAME }
        if (entities.isEmpty())return
        for (entity in entities){
            getEntityAsMap(entity)?:return

            val loc = entity.location.toBlockLocation()

            when(entity.facing){
                BlockFace.NORTH->{
                    loc.add(0.0,0.0,1.0)
                }
                BlockFace.EAST->{
                    loc.add(-1.0,0.0,0.0)
                }
                BlockFace.SOUTH->{
                    loc.add(0.0,0.0,-1.0)
                }
                BlockFace.WEST->{
                    loc.add(1.0,0.0,0.0)
                }
                BlockFace.DOWN->{
                    loc.add(0.0,1.0,0.0)
                }
                BlockFace.UP->{
                    loc.add(0.0,-1.0,0.0)
                }
                else ->{}
            }

            val breakLoc = e.block.location.toBlockLocation()

            if (breakLoc == loc){
                e.isCancelled = true
                e.player.sendMessage(Main.prefix + "§4この額縁は保護されています")
                return
            }
        }
    }

    @EventHandler
    fun entityExplodeEvent(e: EntityExplodeEvent){
        if (e.isCancelled)return
        val list = e.blockList().filter { isProtectedBlock(it.location) }
        if (list.isEmpty())return
        list.forEach {
            e.blockList().remove(it)
        }
    }

    @EventHandler
    fun blockExplodeEvent(e: BlockExplodeEvent){
        if (e.isCancelled)return
        val list = e.blockList().filter { isProtectedBlock(it.location) }
        if (list.isEmpty())return
        list.forEach {
            e.blockList().remove(it)
        }
    }

    @EventHandler
    fun blockPlaceEvent(e: BlockPlaceEvent){
        if (e.isCancelled)return

        if (isProtectedBlock(e.block.location.subtract(0.0,1.0,0.0))){
            e.player.sendMessage(Main.prefix + "§4額縁付近にはブロックは置けません")
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun playerBucketEmptyEvent(e: PlayerBucketEmptyEvent){
        if (e.isCancelled)return
        if (e.bucket == Material.MILK_BUCKET)return
        if (isProtectedBlock(e.block.location.subtract(0.0,1.0,0.0))){
            e.player.sendMessage(Main.prefix + "§4額縁付近にはブロックは置けません")
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun blockFadeEvent(e: BlockFadeEvent){
        if (isProtectedBlock(e.block.location))e.isCancelled = true
    }



    fun getEntityAsMap(entity: Entity): MapView? {
        if (entity !is ItemFrame)return null
        val meta = entity.item.itemMeta as? MapMeta?:return null
        val mapView = meta.mapView?:return null
        if (Main.displayManager.displays.none { it.mapIds.contains(mapView.id) })return null
        return mapView
    }

    private fun isProtectedBlock(location: Location): Boolean {
        val entities = location.subtract(-0.5,-0.5,-0.5).getNearbyEntities(1.5,1.5,1.5).filter { it.type == EntityType.ITEM_FRAME || it.type == EntityType.GLOW_ITEM_FRAME }
        if (entities.isEmpty())return false
        for (entity in entities){
            getEntityAsMap(entity)?:continue

            val loc = entity.location.toBlockLocation()
            loc.add(0.0,-1.0,0.0)

            val breakLoc = location.toBlockLocation()

            if (breakLoc == loc){
                return true
            }
        }
        return false
    }

    private fun isStaff(item: ItemStack): Boolean {
        return !item.type.isAir && item.itemMeta.persistentDataContainer.has(
            NamespacedKey(Main.plugin,"displaystaff"),
            PersistentDataType.INTEGER)
    }
}