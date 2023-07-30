package red.man10.extention

import org.bukkit.inventory.Inventory
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun Inventory.toBase64(): String {
    return try {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)

        // Write the size of the inventory
        dataOutput.writeInt(this.size)

        // Save every element in the list
        for (i in 0 until this.size) {
            dataOutput.writeObject(this.getItem(i))
        }
        // Serialize that array
        dataOutput.close()
        Base64Coder.encodeLines(outputStream.toByteArray())
    } catch (e: java.lang.Exception) {
        throw IllegalStateException("Unable to save item stacks.", e)
    }
}

fun Inventory.fromBase64(data: String) {
    try {
        val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
        val dataInput = BukkitObjectInputStream(inputStream)
        val size = dataInput.readInt()
        for (i in 0 until size) {
            this.setItem(i, dataInput.readObject() as? org.bukkit.inventory.ItemStack)
        }
        dataInput.close()
    } catch (e: java.lang.Exception) {
        throw IllegalStateException("Unable to decode class type.", e)
    }
}