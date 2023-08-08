import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.inventory.ItemStack
import red.man10.extention.getMap

/**
 * このLocationと指定されたBlockFaceに対応する額縁（光る額縁を含む）を取得します。
 *
 * @param face 額縁の方向を指定するBlockFace。
 * @return 額縁が存在すればそのEntityを、存在しなければnullを返します。
 */
fun Location.getItemFrame(face: BlockFace): Entity? {
    return world.getNearbyEntities(this, 1.0, 1.0, 1.0)
        //return world.getNearbyEntities(this, 0.5, 0.5, 0.5)
        .filterIsInstance<ItemFrame>()
        .firstOrNull { it.facing == face }
}

/**
 * 指定されたBlockFaceの方向にある額縁を削除します。
 *
 * @param face 額縁の方向を指定するBlockFace。
 * @param dropItem 額縁の中のアイテムをドロップさせる場合はtrue、ドロップさせずに消去する場合はfalse。
 * @return 操作が正常に行われた場合はtrue、それ以外の場合はfalseを返します。
 */
fun Location.removeFrame(face: BlockFace, dropItem: Boolean = false): Boolean {
    val frame = getItemFrame(face) ?: return false
    if (frame is ItemFrame) {
        frame.remove()
    }
    return true

}

fun Location.getItemStackInFrame(face: BlockFace): ItemStack? {
    val frame = getItemFrame(face) ?: return null
    if (frame is ItemFrame) {
        return frame.item
    }
    return null
}

/**
 * 指定されたLocationとBlockFaceにアイテムフレーム（または光るアイテムフレーム）を設置し、アイテムを設定する関数
 *
 * @param itemStack 設置するアイテムフレームに設定するアイテム。nullの場合、アイテムは設定されない
 * @param face アイテムフレームの設置方向
 * @param glowing trueの場合、光るアイテムフレームを設置する。falseの場合、通常のアイテムフレームを設置する
 */
fun Location.placeItemFrame(face: BlockFace, itemStack: ItemStack? = null, glowing: Boolean = false): Boolean {

    // その場所にすでに額縁がある場合は削除
    //this.removeFrame(face)

    val frameEntity = if (glowing) {
        this.world?.spawnEntity(this, EntityType.GLOW_ITEM_FRAME)
    } else {
        this.world?.spawnEntity(this, EntityType.ITEM_FRAME)
    }

    if (frameEntity is ItemFrame) {
        frameEntity.setFacingDirection(face, true)
        if (itemStack == null)
            return false
        frameEntity.setItem(itemStack, false)
    }
    return true
}

fun Location.placeMap(face: BlockFace, mapId: Int, glowing: Boolean = false): Boolean {
    this.placeItemFrame(face, ItemStack(Material.FILLED_MAP).getMap(mapId), glowing)
    return false
}

