package red.man10.display

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import red.man10.display.commands.Man10DisplayCommand
import red.man10.display.itemframe.IFPListener
import red.man10.display.itemframe.ItemFrameListener


class Main : JavaPlugin(), Listener {
    companion object {
        val version = "2023/7/22"
        var commandSender: CommandSender? = null
        val prefix = "[Man10Display] "
        lateinit var plugin: JavaPlugin
        lateinit var displayManager: DisplayManager
        lateinit var appManager: AppManager
        lateinit var imageManager: ImageManager
        lateinit var protocolManager: ProtocolManager
        lateinit var commandRouter: Man10DisplayCommand
        lateinit var settings: ConfigData
    }

    override fun onEnable() {
        plugin = this
        settings = ConfigData()
        settings.load(this, config)
        imageManager = ImageManager(settings.imagePath)
        protocolManager = ProtocolLibrary.getProtocolManager()
        displayManager = DisplayManager(this)

        commandRouter = Man10DisplayCommand()
        getCommand("mdisplay")!!.setExecutor(commandRouter)
        getCommand("mdisplay")!!.tabCompleter = commandRouter
        getCommand("md")!!.setExecutor(commandRouter)
        getCommand("md")!!.tabCompleter = commandRouter

        //額縁保護用のイベント
        server.pluginManager.registerEvents(ItemFrameListener(), this)
        if (server.pluginManager.getPlugin("ItemFrameProtector") != null) {
            server.pluginManager.registerEvents(IFPListener(), this)
        }


        appManager = AppManager(this)



        info("Man10 Display Plugin Enabled")
    }

    override fun onDisable() {
        displayManager.deinit()
        info("Disabled Man10 Display Plugin")
    }
}
