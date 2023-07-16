package red.man10.display

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

// 通常ログ
fun info(message:String,sender:CommandSender? = null){
    Bukkit.getLogger().info(Main.prefix +message)
    sender?.sendMessage(message)
}
// エラーログ
fun error(message:String,sender:CommandSender? = null) {
    Bukkit.getLogger().severe(Main.prefix +message)
    sender?.sendMessage(message)
}

fun formatNumber(value: Long): String {
    val suffixes = listOf("", "K", "M", "G", "T", "P", "E")
    var convertedValue = value.toDouble()
    var suffixIndex = 0

    while (convertedValue >= 1000 && suffixIndex < suffixes.size - 1) {
        convertedValue /= 1000
        suffixIndex++
    }

    val formattedValue = when {
        convertedValue >= 1000 -> "%.0f".format(convertedValue)
        convertedValue >= 100 -> "%.1f".format(convertedValue)
        else -> "%.2f".format(convertedValue)
    }
    return "$formattedValue${suffixes[suffixIndex]}"
}

fun formatNumberWithCommas(value: Long): String {
    val formattedValue = formatNumber(value)
    val parts = formattedValue.split(".")
    val integerPartWithCommas = parts[0].chunked(3).joinToString(",")
    return if (parts.size > 1) "$integerPartWithCommas.${parts[1]}" else integerPartWithCommas
}
fun showModeTitle(player:Player, title:String, subtitle:String="", fadeIn:Int = 10, stay:Int=100, fadeOut:Int = 10){
    player.sendTitle(title,subtitle,fadeIn,stay,fadeOut)
}
fun showModeTitle(player:Player, title:String, subtitle:String="", keepSec:Double){
    val time = keepSec * 20.0
    player.sendTitle(title,subtitle,10,time.toInt(),10)
}


fun sendActionText(player:Player?,message:String?){
    if(message.isNullOrEmpty())
        return
    if(player ==null)
        return
    if(!player.isOnline)
        return
    val component = TextComponent.fromLegacyText(message)
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component[0])
}

// オフラインのプレイヤーを返す
fun getOfflinePlayer(sender: CommandSender, name:String?): Player?{
    if(name == null){
        error("名前が指定されていません",sender)
        return null
    }
    val player = Bukkit.getOfflinePlayerIfCached(name)
    if(player == null){
        error("プレイヤーが存在しません",sender)
        return null
    }
    return player.player
}
// オンラインのプレイヤーを返す
fun getOnlinePlayer(sender: CommandSender, name:String?): Player?{
    if(name == null){
        error("名前が指定されていません",sender)
        return null
    }
    val player = Bukkit.getPlayer(name)
    if(player == null){
        error("プレイヤーが見つかりません",sender)
        return null
    }
    if(!player.isOnline){
        error("プレイヤーがオンラインではありません",sender)
        return null
    }
    return player
}
fun toRadian(angle: Double): Double {
    return angle * Math.PI / 180f
}

fun sendClickableMessage(player: Player, message: String, command: String) {
    val textComponent = TextComponent(message)
    textComponent.color = ChatColor.GOLD
    textComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
    player.spigot().sendMessage(textComponent)
}
class Utility {

    // 角度->ラジアン

    // ラジアン->角度
    fun toAngle(radian: Double): Double {
        return radian * 180 / Math.PI
    }

    @Throws(IllegalStateException::class)
    fun toBase64(inventory: Inventory): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)

            // Write the size of the inventory
            dataOutput.writeInt(inventory.size)

            // Save every element in the list
            for (i in 0 until inventory.size) {
                dataOutput.writeObject(inventory.getItem(i))
            }

            // Serialize that array
            dataOutput.close()
            Base64Coder.encodeLines(outputStream.toByteArray())
        } catch (e: java.lang.Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }

    @Throws(IOException::class)
    fun fromBase64(data: String?): Inventory? {
        return try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val inventory = Bukkit.getServer().createInventory(null, dataInput.readInt())

            // Read the serialized inventory
            for (i in 0 until inventory.size) {
                inventory.setItem(i, dataInput.readObject() as ItemStack)
            }
            dataInput.close()
            inventory
        } catch (e: ClassNotFoundException) {
            throw IOException("Unable to decode class type.", e)
        }
    }
    @Throws(IllegalStateException::class)
    fun itemStackArrayToBase64(items: Array<ItemStack?>): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            // Write the size of the inventory
            dataOutput.writeInt(items.size)
            // Save every element in the list
            for (i in items.indices) {
                dataOutput.writeObject(items[i])
            }
            // Serialize that array
            dataOutput.close()
            Base64Coder.encodeLines(outputStream.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }

    @Throws(IOException::class)
    fun itemStackArrayFromBase64(data: String?): Array<ItemStack?>? {
        return try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val items = arrayOfNulls<ItemStack>(dataInput.readInt())

            // Read the serialized inventory
            for (i in items.indices) {
                items[i] = dataInput.readObject() as ItemStack
            }
            dataInput.close()
            items
        } catch (e: ClassNotFoundException) {
            throw IOException("Unable to decode class type.", e)
        }
    }


}