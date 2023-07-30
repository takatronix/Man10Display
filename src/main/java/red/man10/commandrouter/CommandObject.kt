package red.man10.commandrouter

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.function.Consumer
import java.util.function.Function

class CommandObject {
    var arguments = ArrayList<CommandArgument?>()
    private var executors = ArrayList<CommandExecutor>()
    private var inlineExecutors = ArrayList<Consumer<CommandData>>()
    private var isInfinity = false
    var permission: String? = null
    private var explanation = ArrayList<String>()
    fun infinity(): CommandObject {
        isInfinity = true
        return this
    }

    fun permission(permission: String): CommandObject {
        this.permission = permission
        return this
    }

    fun explanation(text: String): CommandObject {
        explanation.add(text)
        return this
    }

    fun argument(arg: CommandArgument?): CommandObject {
        arguments.add(arg)
        return this
    }

    fun prefix(prefix: String?): CommandObject {
        arguments.add(
            CommandArgument()
                .allowedString(prefix)
        )
        return this
    }

    fun argument(alias: String?): CommandObject {
        arguments.add(
            CommandArgument()
                .alias(alias)
        )
        return this
    }

    fun argument(alias: String?, vararg allowedStrings: String?): CommandObject {
        arguments.add(
            CommandArgument()
                .alias(alias)
                .allowedString(*allowedStrings)
        )
        return this
    }

    fun argument(alias: String?, function: Function<CommandSender, ArrayList<String>>, strict: Boolean): CommandObject {
        if (strict) {
            arguments.add(
                CommandArgument()
                    .alias(alias)
                    .allowedStringsFunction(function)
            )
        } else {
            arguments.add(
                CommandArgument()
                    .alias(alias)
                    .aliasStringsFunction(function)
            )
        }
        return this
    }

    fun argument(alias: String?, function: Function<CommandSender, ArrayList<String>>): CommandObject {
        this.argument(alias, function, true)
        return this
    }

    fun argument(alias: String?, type: CommandArgumentType): CommandObject {
        arguments.add(
            type.allowedString?.let {
                type.argumentParser?.let { it1 ->
                    CommandArgument()
                        .alias(alias)
                        .allowedStringsFunction(it)
                        .argumentParser(it1)
                }
            }
        )
        return this
    }

    fun executor(event: CommandExecutor): CommandObject {
        executors.add(event)
        return this
    }

    fun inlineExecutor(event: Consumer<CommandData>): CommandObject {
        inlineExecutors.add(event)
        return this
    }

    fun hasPermission(sender: CommandSender?): Boolean {
        return if (permission == null) false else sender!!.hasPermission(permission!!)
    }

    fun matches(args: Array<String>, sender: CommandSender?): Boolean {
        if (args.size < arguments.size)
            return false
        if (args.size > arguments.size && !isInfinity)
            return false
        for (i in args.indices) {
            if (arguments.size - 1 <= i && isInfinity)
                continue
            if (!arguments[i]!!.matches(args[i], sender))
                return false
        }
        return true
    }

    fun validOption(args: Array<String>, sender: CommandSender?): Boolean {
        if (args.size > arguments.size) return false
        for (i in 0 until args.size - 1) {
            if (!arguments[i]!!.matches(args[i], sender)) {
                return false
            }
        }
        return true
    }

    fun execute(obj: CommandData) {
        for (executor in executors) {
            executor.onCommand(obj.sender, obj.command!!, obj.label, obj.args)
        }
        for (event in inlineExecutors) {
            event.accept(obj)
        }
    }

    fun helpText(baseCommand: String?, prefix: String, sender: CommandSender?): Array<BaseComponent> {
        val builder = ComponentBuilder()
        val commandExplanation = StringBuilder()
        for (exp in explanation) {
            commandExplanation.append(exp).append("\n")
        }
        if (explanation.size != 0) commandExplanation.append("\n")
        commandExplanation.append("§e==========Permission==========\n")
        if (permission != null) {
            commandExplanation.append("§d").append(permission).append("\n")
        } else {
            commandExplanation.append("§d").append("なし").append("\n")
        }
        builder.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(commandExplanation.toString())))
        builder.append("$prefix/$baseCommand ")
        for (arg in arguments) {
            val allowedStrings = arg!!.getAllowedStrings(sender)
            if (allowedStrings.size == 1) {
                builder.append(prefix + allowedStrings[0])
            } else {
                if (arg.alias != null) builder.append(prefix + "<" + arg.alias + ">")
            }
            //explanation
            val explanation = StringBuilder()
            for (exp in arg.explanations) {
                explanation.append("§d").append(exp).append("\n")
            }
            if (arg.explanations.size != 0) explanation.append("\n")
            if (allowedStrings.size != 0 && allowedStrings.size != 1) {
                explanation.append("§e==========Available Parameter==========\n")
                for (argument in allowedStrings) {
                    explanation.append("§d").append(argument).append("\n")
                }
            }
            builder.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(explanation.toString())))
            builder.append(" ")
            builder.event(null as HoverEvent?)
        }
        return builder.create()
    }


}
