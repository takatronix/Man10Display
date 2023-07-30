package red.man10.commandrouter

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.function.Function

enum class CommandArgumentType(
    allowedString: Function<CommandSender, ArrayList<String>>,
    argumentParser: Function<String, Boolean>?
) {
    ONLINE_PLAYER(Function { _: CommandSender? ->
        val result = ArrayList<String>()
        for (p in Bukkit.getOnlinePlayers()) {
            result.add(p.name)
        }
        result
    }, null),
    WORLD(Function { _: CommandSender? ->
        val result = ArrayList<String>()
        for (w in Bukkit.getWorlds()) {
            result.add(w.name)
        }
        result
    }, null),
    BOOLEAN(
        Function { _: CommandSender? -> ArrayList(listOf("true", "false")) },
        Function { string: String? ->
            try {
                java.lang.Boolean.parseBoolean(string)
                return@Function true
            } catch (e: Exception) {
                return@Function false
            }
        });

    var allowedString: Function<CommandSender, ArrayList<String>>? = null
    var argumentParser: Function<String, Boolean>? = null

    init {
        this.allowedString = allowedString
        this.argumentParser = argumentParser
    }
}
