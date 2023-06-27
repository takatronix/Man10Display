package red.man10.display.commands

import red.man10.commandrouter.*
import red.man10.display.Main

class Man10DisplayCommand : CommandRouter(Main.plugin,"mdisplay")
{
    init {
        registerCommands()
        registerEvents()
        pluginPrefix = Main.prefix
    }
    fun registerEvents() {
        setNoPermissionEvent { e: CommandData -> e.sender.sendMessage(Main.prefix + "§c§lYou don't have permission") }
        setNoCommandFoundEvent { e: CommandData -> e.sender.sendMessage(Main.prefix + "§c§lThat command does not exist.") }
    }
    fun registerCommands() {

        // reload command
        addCommand(
            CommandObject()
                .argument(CommandArgument().allowedString("reload"))
                .permission("red.man10.display.op")
                .explanation("Reload the config file")
                .executor(ReloadConfigCommand(Main.plugin))
        )

        // create stream command
        addCommand(
            CommandObject()
                .argument(CommandArgument().allowedString("create_stream"))
                .permission("red.man10.display.op")
                .explanation("Create a display for streaming")
                .executor(ReloadConfigCommand(Main.plugin))
        )

        // delete command
        addCommand(
            CommandObject()
                .argument(CommandArgument().allowedString("delete"))
                    .argument("id",)
                .permission("red.man10.display.op")
                .explanation("Delete display with specified id")
                .executor(ReloadConfigCommand(Main.plugin))
        )



    }
}