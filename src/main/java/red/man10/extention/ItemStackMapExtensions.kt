package red.man10.extention

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta

/**
 * ItemStackが地図であるかどうかを判定します。
 *
 * @return このItemStackが地図の場合はtrue、そうでない場合はfalse
 */
fun ItemStack.isMap(): Boolean {
    return this.type == Material.MAP
}

fun ItemStack.isFilledMap(): Boolean {
    return this.type == Material.FILLED_MAP
}

fun ItemStack.isMapOrFilledMap(): Boolean {
    return this.isMap() || this.isFilledMap()
}

/**
 * ItemStackから地図IDを取得します。
 *
 * @return 地図のID。このItemStackが地図でない場合はnull
 */
fun ItemStack.getMapId(): Int? {
    if (!this.isFilledMap()) {
        return null
    }
    val mapMeta = this.itemMeta as MapMeta
    return mapMeta.mapView?.id
}

fun ItemStack.setMapId(mapId: Int): ItemStack {
    if (!this.isFilledMap()) {
        return this
    }
    val mapMeta = this.itemMeta as MapMeta
    mapMeta.mapView = Bukkit.getServer().getMap(mapId)
    this.itemMeta = mapMeta
    return this
}

fun ItemStack.getMap(mapId: Int): ItemStack {
    val item = ItemStack(Material.FILLED_MAP)
    val mapMeta = this.itemMeta as MapMeta
    mapMeta.mapView = Bukkit.getServer().getMap(mapId)
    item.itemMeta = mapMeta
    return item
}