package red.man10.extention

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

inline fun <reified T : Any> PersistentDataContainer.set(
    plugin: Plugin,
    key: String,
    type: PersistentDataType<T, T>,
    value: T
) {
    set(NamespacedKey(plugin, key), type, value)
}

inline fun <reified T> PersistentDataContainer.get(plugin: Plugin, key: String, type: PersistentDataType<T, T>): T? {
    return get(NamespacedKey(plugin, key), type)
}

inline fun <reified T : Any> ItemStack.setPersistentData(
    plugin: Plugin,
    key: String,
    type: PersistentDataType<T, T>,
    value: T
) {
    val meta = this.itemMeta
    meta.persistentDataContainer.set(plugin, key, type, value)
    this.itemMeta = meta
}

inline fun <reified T> ItemStack.getPersistentData(plugin: Plugin, key: String, type: PersistentDataType<T, T>): T? {
    return this.itemMeta.persistentDataContainer.get(plugin, key, type)
}