package red.man10.display

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

// 通常ログ
fun info(message: String, sender: CommandSender? = null) {
    Bukkit.getLogger().info(Main.prefix + message)
    sender?.sendMessage(message)
}

// エラーログ
fun error(message: String, sender: CommandSender? = null) {
    Bukkit.getLogger().severe(Main.prefix + message)
    sender?.sendMessage(message)
}
