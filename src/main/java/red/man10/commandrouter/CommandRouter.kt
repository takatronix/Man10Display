package red.man10.commandrouter

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

abstract class CommandRouter(plugin: JavaPlugin, private val commandName: String) : CommandExecutor, TabCompleter {
    private var onNoCommandFoundEvent: Consumer<CommandData>? = null
    private var onNoPermissionEvent: Consumer<CommandData>? = null
    var pluginPrefix: String? = null

    init {
        plugin.getCommand(commandName)!!.setExecutor(this)
        plugin.getCommand(commandName)!!.tabCompleter = this
        addCommand(
            CommandObject()
                .argument(CommandArgument().allowedString("help"))
                .inlineExecutor { data: CommandData -> help(data) })
    }

    fun addCommand(command: CommandObject?) {
        if (!commands.containsKey(commandName))
            commands[commandName] = ArrayList()
        if (commands[commandName]!!.contains(command)) return
        commands[commandName]!!.add(command)
    }

    open fun setNoCommandFoundEvent(event: Consumer<CommandData>) {
        onNoCommandFoundEvent = event
    }

    open fun setNoPermissionEvent(event: Consumer<CommandData>) {
        onNoPermissionEvent = event
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val commandData = CommandData(sender, command, label, args)
        for (commandObject in commands[commandName]!!) {
            if (commandObject!!.matches(args, sender)) {

                //permission
                if (commandObject.permission != null) {
                    val hasPermission = sender.hasPermission(commandObject.permission!!)
                    if (!hasPermission) {
                        onNoPermissionEvent?.accept(commandData)
                        return false
                    }
                }
                commandObject.execute(commandData)
                return true
            }
        }
        if (onNoCommandFoundEvent != null) onNoCommandFoundEvent!!.accept(commandData)
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        val result: MutableList<String> = ArrayList()
        for (commandObject in commands[commandName]!!) {
            if (commandObject!!.permission != null) {
                val hasPermission = sender.hasPermission(commandObject.permission!!)
                if (!hasPermission)
                    continue
            }
            if (commandObject.validOption(args, sender)) {
                val argument = commandObject.arguments[args.size - 1] ?: continue
                result.addAll(argument.getAliasStrings(sender))
                result.addAll(argument.getAllowedStrings(sender).filterNotNull())
            }
        }
        return result
    }

    //help
    fun help(data: CommandData) {
        data.sender.sendMessage("§e==========$pluginPrefix§e===========")
        for (obj in commands[commandName]!!) {
            if (obj!!.hasPermission(data.sender))
                data.sender.sendMessage(*obj.helpText(data.label, "§d", data.sender))
        }
        data.sender.sendMessage("§e===================================")
    }

    companion object {
        var commands = ConcurrentHashMap<String, ArrayList<CommandObject?>>()
        fun executeSCommand(executor: CommandSender, command: String) {
            val splitCommands = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (splitCommands.isEmpty()) return
            val label = splitCommands[0]
            val args = splitCommands.copyOfRange(1, splitCommands.size)
            val commandData = CommandData(executor, Bukkit.getPluginCommand(label), label, args)
            for (commandObject in commands[label]!!) {
                if (commandObject!!.matches(args, executor)) {
                    //permission
                    if (commandObject.permission != null) {
                        val hasPermission = executor.hasPermission(commandObject.permission!!)
                        if (!hasPermission) continue
                    }
                    commandObject.execute(commandData)
                    return
                }
            }
        }
    }
}
