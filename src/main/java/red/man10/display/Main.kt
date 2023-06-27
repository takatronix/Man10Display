package red.man10.display

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

    }

    override fun onEnable() {
        plugin = this
        val commandRouter = Man10DisplayCommand()
        getCommand("mdisplay")!!.setExecutor(commandRouter)
        getCommand("mdisplay")!!.tabCompleter = commandRouter



        info("Man10 Display Plugin Enabled")
    }

    override fun onDisable() {
        info("Disabling Man10 Display Plugin")

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
