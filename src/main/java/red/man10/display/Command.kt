package red.man10.display

import org.bukkit.Bukkit.getServer
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView



object Command : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if(!sender.hasPermission("red.man10.display.op")){
            sender.sendMessage("You do not have permission to use this command.")
            return  false
        }

        if(args.isEmpty()){
            showHelp(label,sender)
            return true
        }

        when(args[0]){
            "map"-> map(label,sender,args)
            "test" -> test(label,sender,args)
        }

        return false
    }

    fun showHelp(label: String, sender: CommandSender){
        sender.sendMessage("§e§l===========$label§e§l===========")
        sender.sendMessage("§e/$label create_stream [name] [width] [height] [port]")
        sender.sendMessage("§e/$label create_image  [name] [width] [height] [url]")
        sender.sendMessage("§e/$label get_map [name]  §9Get a map for a given name")
        sender.sendMessage("§e/$label delete [name]   §9Delete map with specified name")
        sender.sendMessage("§e§l============================")
    }


    private fun test(label:String, sender: CommandSender, args: Array<out String>){
    }

    // タブ補完
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>?): List<String>? {

        if(args?.size == 1){
            return listOf("create_stream","create_image","get_map","delete")
        }
        when(args?.get(0)){
            /*
            "set" -> return onTabSet(args)
            "kit" -> return onTabKit(args,alias)
            "location" -> return onTabLocation(args,alias)
            "config" -> return onTabConfig(args)
            "vision" -> return onTabVision(args)

             */
        }
        return null
    }

    private fun createMaps(display: Display, player: Player){

        for(y in 0 until display.height){
            for(x in 0 until display.width){
                val mapView = getServer().createMap(player.world)
                mapView.scale = MapView.Scale.CLOSEST
                mapView.isUnlimitedTracking = true
                //for (renderer in mapView.renderers) {
                //    mapView.removeRenderer(renderer)
               // }

                val itemStack = ItemStack(Material.FILLED_MAP)
                val mapMeta = itemStack.itemMeta as MapMeta
                mapMeta.setMapView(mapView)
                itemStack.setItemMeta(mapMeta)
                // 名称に　display.name + x + yを入れる

                // mapidをdisplayに保存
                display.mapIds.add(mapView.id)
            }
        }

    }


    private fun map(label:String,sender: CommandSender,args: Array<out String>){
        val player = sender as Player
        var x = 0
        var y = 0

        val command = args[0]
/*

        for (i in 0 until Main.configData.mapSize) {
            var mapView: MapView? = null

            // Check for existing maps
            for (screenPart in screens) {
                if (screenPart.partId == i) {
                    mapView = getServer().getMap(screenPart.mapId)
                }
            }

            // Create new map if none exists
            if (mapView == null) {
                mapView = getServer().createMap(player.world)
                for (j in 0 until BUFFER_MAP_COUNT - 1) getServer().createMap(player.world) // Create extra buffer maps
                screens.add(ScreenPart(mapView.id, i))
            }
            mapView.scale = MapView.Scale.CLOSEST
            mapView.isUnlimitedTracking = true
            for (renderer in mapView.renderers) {
                mapView.removeRenderer(renderer)
            }
            val itemStack = ItemStack(Material.FILLED_MAP)
            val mapMeta = itemStack.itemMeta as MapMeta
            mapMeta.setMapView(mapView)
            itemStack.setItemMeta(mapMeta)
            val manager = Main.mapManager

            manager.saveImage(mapView.id, i)

            // mapautoなら目の前に配置
                             if (command == "mapauto") {
                var location = player.getTargetBlock(10)!!.location
                val facing = player.getTargetBlockFace(10)!!.oppositeFace
                location = location.add(
                    facing.oppositeFace.modX.toDouble(),
                    facing.oppositeFace.modY.toDouble(),
                    facing.oppositeFace.modZ.toDouble()
                )
                location = location.add((-x * facing.modZ).toDouble(), -y.toDouble(), (x * facing.modX).toDouble())
                val itemFrame: ItemFrame = player.world.spawn(location, GlowItemFrame::class.java)
                itemFrame.isVisible = false
                itemFrame.setFacingDirection(facing.oppositeFace)
                itemFrame.setItem(itemStack)
            } else {
                player.world.dropItem(player.location, itemStack)
            }
            x++
            if (x >= Main.configData.mapWidth) {
                x = 0
                y++
            }
        }

 */
    }
}