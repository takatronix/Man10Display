package red.man10.commandrouter

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandData(var sender: CommandSender, var command: Command?, var label: String, var args: Array<String>)
