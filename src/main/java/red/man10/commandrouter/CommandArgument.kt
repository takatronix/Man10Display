package red.man10.commandrouter

import org.bukkit.command.CommandSender
import java.util.function.Function


class CommandArgument {
    var alias: String? = null
    private val allowedStrings = ArrayList<String?>()
    private var allowedStringsFunction: Function<CommandSender, ArrayList<String>>? = null
    private var aliasStringsFunction: Function<CommandSender, java.util.ArrayList<String>>? = null
    private var argumentParser: Function<String, Boolean>? = null

    var explanations = ArrayList<String>()
    fun explanation(vararg text: String): CommandArgument {
        explanations.addAll(listOf(*text))
        return this
    }

    fun allowedString(vararg string: String?): CommandArgument {
        allowedStrings.addAll(listOf(*string))
        return this
    }

    fun allowedStringsFunction(function: Function<CommandSender, ArrayList<String>>): CommandArgument {
        allowedStringsFunction = function
        return this
    }

    fun argumentParser(function: Function<String, Boolean>): CommandArgument {
        argumentParser = function
        return this
    }

    fun alias(alias: String?): CommandArgument {
        this.alias = alias
        return this
    }

    fun aliasStringsFunction(function: Function<CommandSender, ArrayList<String>>): CommandArgument {
        aliasStringsFunction = function
        return this
    }

    fun getAllowedStrings(sender: CommandSender?): ArrayList<String?> {
        val results = ArrayList(allowedStrings)
        if (allowedStringsFunction != null) {
            try {
                results.addAll(allowedStringsFunction!!.apply(sender!!))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return results
    }

    fun getAliasStrings(sender: CommandSender?): ArrayList<String> {
        val results = ArrayList<String>()
        if (alias != null) results.add(alias!!)
        if (aliasStringsFunction != null) {
            try {
                results.addAll(aliasStringsFunction!!.apply(sender!!))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return results
    }

    fun matches(arg: String?, sender: CommandSender?): Boolean {
        val allowedStrings = getAllowedStrings(sender)
        if (allowedStrings.isEmpty() && argumentParser == null)
            return true
        for (testingAllowedString in allowedStrings) {
            if (testingAllowedString.equals(arg, ignoreCase = true))
                return true
        }
        if (argumentParser != null) {
            try {
                val result = argumentParser!!.apply(arg!!)
                if (!result) return false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        return false
    }
}