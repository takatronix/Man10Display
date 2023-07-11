package red.man10.display

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.commands.Man10DisplayCommand


class Main : JavaPlugin() ,Listener {

    companion object {
        val version = "2023/4/10"
        var commandSender: CommandSender? = null
        val prefix = "§e[Man10Display]"

        lateinit var plugin: JavaPlugin
        lateinit var displayManager: DisplayManager
        lateinit var protocolManager: ProtocolManager
        lateinit var commandRouter:Man10DisplayCommand
    }

    override fun onEnable() {
        plugin = this
        protocolManager = ProtocolLibrary.getProtocolManager()
        displayManager = DisplayManager(this)

        commandRouter = Man10DisplayCommand()
        getCommand("mdisplay")!!.setExecutor(commandRouter)
        getCommand("mdisplay")!!.tabCompleter = commandRouter
        saveDefaultConfig()

        info("Man10 Display Plugin Enabled")
    }

    override fun onDisable() {
        info("Disabling Man10 Display Plugin")
        displayManager.deinit()

    }


    fun saveConfigData(configData: ConfigData) {
        plugin.config.set("broadcast", configData.broadcast)
        plugin.config.set("switchTime", configData.switchTime)
        plugin.saveConfig()

        showConfigData()
    }

    fun showConfigData(sender: CommandSender? = null) {
   //     info("broadcast:${Main.configData.broadcast}")
    //    info("switchTime:${Main.configData.switchTime}")
    }
}
