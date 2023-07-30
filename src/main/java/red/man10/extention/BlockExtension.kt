package red.man10.extention

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemFrame

fun Block.getItemFrame(face: BlockFace): ItemFrame? {
    val relative = this.getRelative(face)
    val entities = relative.chunk.entities
    return entities.firstOrNull { entity ->
        entity is ItemFrame && entity.location.block == relative
    } as? ItemFrame
}
