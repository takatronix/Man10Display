package red.man10.display.commands.logic

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.Main
import red.man10.extention.setPersistentData
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CreateTicketCommand(private var plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            if (sender !is Player) {
                sender.sendMessage(Main.prefix + "§c§lThis command can only be executed by players.")
                return true
            }
            val type = args[1]
            var data = args[2]

            val itemInHand = sender.inventory.itemInMainHand

            // もし手にアイテムがないならエラーメッセージ
            if (sender.inventory.itemInMainHand.type.isAir) {
                sender.sendMessage(Main.prefix + "§c§lYou must hold an item in your hand.")
                return true
            }

            // typeが開始時刻終了時刻ならdataを時刻としてパースする
            if (type == "start" || type == "end") {
                val unixTime = convertToUnixTime(data)
                if (unixTime == null) {
                    sender.sendMessage(Main.prefix + "§c§lInvalid date format. reset the setting")
                }
                data = unixTime.toString()
            }
            val meta: ItemMeta = itemInHand.itemMeta
            itemInHand.itemMeta = meta

            when (type) {
                "count" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.ticket.count",
                    PersistentDataType.LONG,
                    data.toLong()
                )

                "start" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.ticket.start",
                    PersistentDataType.LONG,
                    data.toLong()
                )

                "end" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.ticket.end",
                    PersistentDataType.LONG,
                    data.toLong()
                )

                "player" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.ticket.player",
                    PersistentDataType.STRING,
                    data
                )

                "command" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.ticket.command",
                    PersistentDataType.STRING,
                    data
                )
                "op_command" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.ticket.op_command",
                    PersistentDataType.STRING,
                    data
                )

                "data" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.ticket.data",
                    PersistentDataType.STRING,
                    data
                )

                "key" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.app.key",
                    PersistentDataType.STRING,
                    data
                )

                "macro" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.app.macro",
                    PersistentDataType.STRING,
                    data
                )

                "image" -> itemInHand.setPersistentData(
                    Main.plugin,
                    "man10display.app.image",
                    PersistentDataType.STRING,
                    data
                )
            }

        } catch (e: Exception) {
            sender.sendMessage(Main.prefix + "§c§l ${e.message}")
            return true
        }
        return true
    }
}

fun convertToUnixTime(dateString: String): Long? {
    val formats = listOf(
        "yyyy",
        "yyyy:MM",
        "yyyy:MM:dd",
        "yyyy:MM:dd HH",
        "yyyy:MM:dd HH:mm",
        "yyyy:MM:dd HH:mm:ss",
        "yyyy/M/d",
        "yyyy/MM/dd",
        "yyyy/MM/dd HH",
        "yyyy/MM/dd HH:mm",
        "yyyy/MM/dd HH:mm:ss"
    )

    for (format in formats) {
        try {
            val formatter = DateTimeFormatter.ofPattern(format)
            val dateTime = LocalDateTime.parse(dateString, formatter)
            return dateTime.toEpochSecond(ZoneOffset.UTC)
        } catch (e: DateTimeParseException) {
            // フォーマットが一致しない場合は次のフォーマットを試す
        }
    }
    return null
}
