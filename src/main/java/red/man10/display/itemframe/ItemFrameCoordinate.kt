package red.man10.display.itemframe

import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.floor

class ItemFrameCoordinate {
    companion object {
        fun calculatePixelCoordinate(
            face: BlockFace?,
            rayVector: Vector?,
            collisionLocation: Location?
        ): Pair<Double, Double> {
            if (face == null || rayVector == null || collisionLocation == null)
                return Pair(0.0, 0.0)

            val t = when (face) {
                BlockFace.EAST, BlockFace.WEST -> {
                    rayVector.x
                }

                BlockFace.SOUTH, BlockFace.NORTH -> {
                    rayVector.z
                }

                BlockFace.UP, BlockFace.DOWN -> {
                    rayVector.y
                }

                else -> {
                    1.0
                }
            }
            val frameCollisionLocation = collisionLocation.clone().add(rayVector.clone().multiply(abs(1.0 / 16.0 / t)))

            val height = floor(
                if (face == BlockFace.UP || face == BlockFace.DOWN) {
                    frameCollisionLocation.x - collisionLocation.blockX
                } else {
                    1 - (frameCollisionLocation.y - collisionLocation.blockY)
                } * 128.0
            )

            val width = floor(
                when (face) {
                    BlockFace.SOUTH -> frameCollisionLocation.x - collisionLocation.blockX
                    BlockFace.NORTH -> 1 - (frameCollisionLocation.x - collisionLocation.blockX)
                    BlockFace.EAST -> 1 - (frameCollisionLocation.z - collisionLocation.blockZ)
                    BlockFace.WEST -> frameCollisionLocation.z - collisionLocation.blockZ
                    else -> 0.0
                } * 128.0
            )
            return Pair(width, height)
        }

        // 額縁との衝突点の計算のための係数
        fun calculateFrameDiffMultiplier(face: BlockFace?, rayVector: Vector?): Double {
            if (face == null || rayVector == null)
                return 0.0
            val t = when (face) {
                BlockFace.EAST, BlockFace.WEST -> {
                    rayVector.x
                }

                BlockFace.SOUTH, BlockFace.NORTH -> {
                    rayVector.z
                }

                BlockFace.UP, BlockFace.DOWN -> {
                    rayVector.y
                }

                else -> {
                    1.0
                }
            }
            return abs(1.0 / 16.0 / t)
        }
    }
}