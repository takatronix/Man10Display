package red.man10.display.commands

import red.man10.commandrouter.CommandData
import red.man10.commandrouter.CommandObject
import red.man10.commandrouter.CommandRouter
import red.man10.display.Display
import red.man10.display.Main
import red.man10.display.commands.logic.*
import red.man10.display.macro.MacroEngine

class Man10DisplayCommand : CommandRouter(Main.plugin, "mdisplay") {
    init {
        registerCommands()
        registerEvents()
        pluginPrefix = Main.prefix
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
                .permission("red.man10.display.reload")
                .explanation("Reload the config file")
                .executor(ReloadCommand(Main.plugin))
        )

        // create command
        addCommand(
            CommandObject()
                .prefix("create")
                .argument("[new_display_name]")
                .argument("[x_size(1-24)]")
                .argument("[y_size(1-24)]")
                .argument("port(0(disable)-65535)")
                .permission("red.man10.display.create")
                .explanation("Create a display")
                .executor(CreateCommand(Main.plugin))
        )
        // delete command
        addCommand(
            CommandObject()
                .prefix("delete")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.delete")
                .explanation("Delete display with specified id")
                .executor(DeleteCommand(Main.plugin))
        )
        // save command
        addCommand(
            CommandObject()
                .prefix("save")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.save")
                .explanation("save display with specified id")
                .executor(SaveCommand(Main.plugin))
        )
        // map command
        addCommand(
            CommandObject()
                .prefix("map")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.map")
                .explanation("Get maps with specified id")
                .executor(MapCommand(Main.plugin))
        )
        // list command
        addCommand(
            CommandObject()
                .prefix("list")
                .permission("red.man10.display.list")
                .explanation("Show display list")
                .executor(ListCommand(Main.plugin))
        )
        // info command
        addCommand(
            CommandObject()
                .prefix("info")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.info")
                .explanation("Show display information")
                .executor(InfoCommand(Main.plugin))
        )
        // stats command
        addCommand(
            CommandObject()
                .prefix("stats")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.stats")
                .explanation("Show statistics")
                .executor(StatsCommand(Main.plugin))
        )

        // itemframe wand command
        addCommand(
            CommandObject()
                .prefix("wand")
                .permission("red.man10.display.wand")
                .explanation("Get wand for item frame")
                .executor(WandCommand(Main.plugin))
        )
        // red.man10.extention.set command
        addCommand(
            CommandObject()
                .prefix("set")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .argument("[setting keyword]") { _ -> Display.parameterKeys }
                .argument("value")
                .permission("red.man10.display.set")
                .explanation("Set parameter")
                .executor(SetCommand(Main.plugin))
        )
        // red.man10.extention.teleport command
        addCommand(
            CommandObject()
                .prefix("tp")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.teleport")
                .explanation("Teleport to display")
                .executor(TeleportCommand(Main.plugin))
        )
        // run command
        addCommand(
            CommandObject()
                .prefix("run")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .argument("[macro_name]") { _ -> MacroEngine.macroList }
                .permission("red.man10.display.run")
                .explanation("run macro")
                .executor(RunCommand(Main.plugin))
        )
        // stop command
        addCommand(
            CommandObject()
                .prefix("stop")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.stop")
                .explanation("stop display")
                .executor(StopCommand(Main.plugin))
        )
        // stop all command
        addCommand(
            CommandObject()
                .prefix("stopall")
                .permission("red.man10.display.sopall")
                .explanation("stop display")
                .executor(StopAllCommand(Main.plugin))
        )
        // clear command
        addCommand(
            CommandObject()
                .prefix("clear")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.clear")
                .explanation("clear display")
                .executor(ClearCommand(Main.plugin))
        )

        // reset command
        addCommand(
            CommandObject()
                .prefix("reset")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.reset")
                .explanation("Set parameter")
                .executor(ResetCommand(Main.plugin))
        )
        // refresh command
        addCommand(
            CommandObject()
                .prefix("refresh")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.refresh")
                .explanation("Refresh display")
                .executor(RefreshCommand(Main.plugin))
        )
        // place item frames command
        addCommand(
            CommandObject()
                .prefix("place")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.place")
                .explanation("Create a display where you are looking at")
                .executor(PlaceCommand(Main.plugin))
        )
        // place item frames command
        addCommand(
            CommandObject()
                .prefix("place_growing")
                .argument("[display_name]") { _ -> Main.displayManager.displayNames }
                .permission("red.man10.display.place")
                .explanation("Create a growing display where you are looking at")
                .executor(PlaceGrowingCommand(Main.plugin))
        )
        // remove item frames command
        addCommand(
            CommandObject()
                .prefix("remove")
                .permission("red.man10.display.remove")
                .explanation("Remove display where you are looking at")
                .executor(RemoveCommand(Main.plugin))
        )
        addCommand(
            CommandObject()
                .prefix("create_pen")
                .argument("[pen_width(1-30)]")
                .argument("[color_code(#000000)]")
                .permission("red.man10.display.create_pen")
                .explanation("Make the item in your hand into a pen.")
                .executor(CreatePenCommand(Main.plugin))
        )
        addCommand(
            CommandObject()
                .prefix("create_ticket")
                .argument("[type]") {
                    arrayListOf(
                        "start",
                        "end",
                        "player",
                        "command",
                        "op_command",
                        "data",
                        "macro",
                        "key",
                        "image"
                    )
                }
                .argument("[value]")
                .permission("red.man10.display.create_ticket")
                .explanation("Set item expiration dates, data, commands, etc.")
                .executor(CreateTicketCommand(Main.plugin))
        )
        addCommand(
            CommandObject()
                .prefix("create_photo")
                .argument("[image_url]")
                .permission("red.man10.display.create_photo")
                .explanation("Create a photo item")
                .executor(CreatePhotoCommand(Main.plugin))
        )
        addCommand(
            CommandObject()
                .prefix("create_app")
                .argument("[macro_name]") { _ -> MacroEngine.macroList }
                .permission("red.man10.display.create_app")
                .explanation("Create a growing display where you are looking at")
                .executor(CreateAppCommand(Main.plugin))
        )

        addCommand(
            CommandObject()
                .prefix("test")
                .permission("red.man10.display.op")
                .explanation("test")
                .executor(TestCommand(Main.plugin))
        )
    }

}