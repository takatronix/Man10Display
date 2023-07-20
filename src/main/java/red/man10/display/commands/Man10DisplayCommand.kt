package red.man10.display.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.commandrouter.*
import red.man10.display.Main
import red.man10.display.commands.logic.*

class Man10DisplayCommand : CommandRouter( Main.plugin,"mdisplay")
{
    init {
        registerCommands()
        registerEvents()
        pluginPrefix = Main.prefix
    }

    private fun getTargetBlockCoordinatesArgument(commandSender: CommandSender, range: Int): ArrayList<String>{
        val player = commandSender as Player
        val block = player.getTargetBlock(range) ?: return arrayListOf("None", "None", "None")
        var x = block.location.blockX
        var y = block.location.blockY
        var z = block.location.blockZ
        val orientation = player.getTargetBlockFace(range) ?: return arrayListOf("None", "None", "None")
        val modX = orientation.modX
        val modY = orientation.modY
        val modZ = orientation.modZ
        x += modX
        y += modY
        z += modZ
        return arrayListOf(x.toString(), y.toString(), z.toString())
    }

    private fun registerEvents() {
        setNoPermissionEvent { e: CommandData -> e.sender.sendMessage(Main.prefix + "§c§lYou don't have permission") }
        setNoCommandFoundEvent { e: CommandData -> e.sender.sendMessage(Main.prefix + "§c§lThat command does not exist") }
    }
    private fun registerCommands() {

        // reload command
        addCommand(
            CommandObject()
                .prefix("reload")
                .permission("red.man10.display.op")
                .explanation("Reload the config file")
                .executor(ReloadConfigCommand(Main.plugin))
        )

        // create stream command
        addCommand(
            CommandObject()
                .prefix("create_stream")
                .argument("name").argument("x_size(1-24)").argument("y_size(1-24)").argument("port(1-65535)")
                .permission("red.man10.display.op")
                .explanation("Create a display for streaming")
                .executor(CreateStreamCommand(Main.plugin))
        )
        // delete command
        addCommand(
            CommandObject()
                .prefix("delete")
                .argument("display_name") { _ -> Main.displayManager.names }
                .permission("red.man10.display.op")
                .explanation("Delete display with specified id")
                .executor(DeleteCommand(Main.plugin))
        )
        // save command
        addCommand(
            CommandObject()
                .prefix("save")
                .argument("display_name") { _ -> Main.displayManager.names }
                .permission("red.man10.display.op")
                .explanation("save display with specified id")
                .executor(SaveCommand(Main.plugin))
        )
        // map command
        addCommand(
            CommandObject()
                .prefix("map")
                .argument("display_name") { _ -> Main.displayManager.names }
                .permission("red.man10.display.op")
                .explanation("Get maps with specified id")
                .executor(MapCommand(Main.plugin))
        )
        // list command
        addCommand(
            CommandObject()
                .prefix("list")
                .permission("red.man10.display.op")
                .explanation("Show display list")
                .executor(ListCommand(Main.plugin))
        )
        // info command
        addCommand(
            CommandObject()
                .prefix("info")
                .argument("display_name") { _ -> Main.displayManager.names }
                .permission("red.man10.display.op")
                .explanation("Show display information")
                .executor(InfoCommand(Main.plugin))
        )
        // itemframe staff command
        addCommand(
            CommandObject()
                .prefix("staff")
                .permission("red.man10.display.op")
                .explanation("Give staff for break item frame")
                .executor(ItemFrameRemoveStaffCommand(Main.plugin))
        )
        // set command
        addCommand(
            CommandObject()
                .prefix("set")
                .argument("display_name") { _ -> Main.displayManager.names }
                .argument("[setting keyword]") { _ -> Main.displayManager.parameterKeys }
                .argument("value")
                .permission("red.man10.display.op")
                .explanation("Set parameter")
                .executor(SetCommand(Main.plugin))
        )
        // reset command
        addCommand(
            CommandObject()
                .prefix("reset")
                .argument("display_name") { _ -> Main.displayManager.names }
                .permission("red.man10.display.op")
                .explanation("Set parameter")
                .executor(ResetCommand(Main.plugin))
        )
        // place item frames command
        addCommand(
            CommandObject()
                    .prefix("place")
                    .argument("display_name") { _ -> Main.displayManager.names }
                    .argument("x1", {c -> arrayListOf(getTargetBlockCoordinatesArgument(c, 30)[0])}, false)
                    .argument("y1", {c -> arrayListOf(getTargetBlockCoordinatesArgument(c, 30)[1])}, false)
                    .argument("z1", {c -> arrayListOf(getTargetBlockCoordinatesArgument(c, 30)[2])}, false)
                    .argument("x2", {c -> arrayListOf(getTargetBlockCoordinatesArgument(c, 30)[0])}, false)
                    .argument("y2", {c -> arrayListOf(getTargetBlockCoordinatesArgument(c, 30)[1])}, false)
                    .argument("z2", {c -> arrayListOf(getTargetBlockCoordinatesArgument(c, 30)[2])}, false)
                    .argument("direction", "positive", "negative")
                    .permission("red.man10.display.op")
                    .explanation("Place item frames")
                    .executor(PlaceCommand(Main.plugin))
        )

    }

}