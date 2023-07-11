package red.man10.display.commands.logic

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main

class PlaceCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try{
            val player = sender as Player
            val name = args[1]
            val display = Main.displayManager.getDisplay(name)
            if(display == null){
                sender.sendMessage(Main.prefix + "§c§l $name does not exist")
                return false
            }
            val x1 = args[2].toInt()
            val y1 = args[3].toInt()
            val z1 = args[4].toInt()
            val x2 = args[5].toInt()
            val y2 = args[6].toInt()
            val z2 = args[7].toInt()
            val direction = args[8]

            if(x1 == x2 && y1 == y2 && z1 == z2){
                sender.sendMessage(Main.prefix + "§c§lPlease select a different location for the 2 points")
                return false
            }
            if(!(x1 == x2 || z1 == z2)){
                sender.sendMessage(Main.prefix + "§c§lThe selected area must be a plane")
                return false
            }

            val directionOfPlane = if(x1 == x2) "x" else "z"

            val xMax = x1.coerceAtLeast(x2)
            val xMin = x1.coerceAtMost(x2)
            val zMax = z1.coerceAtLeast(z2)
            val zMin = z1.coerceAtMost(z2)
            val yMax = y1.coerceAtLeast(y2)
            val yMin = y1.coerceAtMost(y2)

            // loop through the area and place the blocks loop from the left top to the right bottom
            // start from y then depending on the direction of the plane, loop through x or z
            // if the direction is x, loop through x first then z
            val itemFrameCoordinates = mutableListOf<Location>()
            for(y in yMax downTo yMin) {
                val isPosDirection = direction == "positive"
                val range = if (directionOfPlane == "x") {
                    if(isPosDirection) zMin..zMax else zMax downTo zMin
                } else {
                    if(isPosDirection) xMax downTo xMin else xMin..xMax
                }
                for(index in range) {
                    val coordinate = if (directionOfPlane == "x") {
                        Location(player.world, xMin.toDouble(), y.toDouble(), index.toDouble())
                    } else {
                        Location(player.world, index.toDouble(), y.toDouble(), zMin.toDouble())
                    }
                    itemFrameCoordinates.add(coordinate)
                }
            }


            val maps: ArrayList<ItemStack> = Main.displayManager.getMaps(display)
            if(maps.size != itemFrameCoordinates.size){
                sender.sendMessage(Main.prefix + "§c§The size of the area does not match the number of maps")
                return false
            }
            // loop through the item frame coordinates and
            // if there is not item frame in the coordinates skip
            // if there is an empty item frame in the coordinates, place the map in the item frame
            for(i in 0 until itemFrameCoordinates.size){
                // if orientation is x, check nearby entities with x = 1
                // if orientation is z, check nearby entities with z = 1
                val x = if(directionOfPlane == "x") 1.0 else 0.0
                val z = if(directionOfPlane == "x") 0.0 else 1.0
                for(entity in itemFrameCoordinates[i].add((1-x)/2, 0.5, (1-z)/2).getNearbyEntities(x, 0.2, z)){
                    if(entity.type == EntityType.ITEM_FRAME || entity.type == EntityType.GLOW_ITEM_FRAME){
                        entity as ItemFrame
                        entity.setItem(maps[i])
                        break
                    }
                }
            }
            sender.sendMessage(Main.prefix + "§a§l $name placed")


        }catch (e:Exception){
            sender.sendMessage(Main.prefix + "§c§l{e.message}")
            return true
        }
        return true
    }
}