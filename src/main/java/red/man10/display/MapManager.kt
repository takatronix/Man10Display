package red.man10.display

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level


class MapManager : Listener {
    private val dataFile = CustomFile("map.yml")
    private val savedImages: MutableMap<Int, Int> = HashMap()

    fun init() {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)
        loadImages()
    }

    @EventHandler
    fun onMapInitEvent(event: MapInitializeEvent) {
        Bukkit.getLogger().info("MapInitializeEvent:" + event.map.id)
        if (hasImage(event.map.id)) {
            val view = event.map
            for (renderer in view.renderers)
                view.removeRenderer(renderer)
            view.scale = MapView.Scale.CLOSEST
            view.isTrackingPosition = false
            view.isUnlimitedTracking = false

            Bukkit.getLogger().info("レンダリング登録:" + event.map.id)
            view.addRenderer(object : MapRenderer(true) {
                override fun render(mapView: MapView, mapCanvas: MapCanvas, player: Player) {
                    val scr = ScreenPart(view.id,getImage(view.id)!!)
           //         Main.videoCapture?.renderCanvas(scr, mapCanvas)
                }
            })

        }
    }

    /***
     * 新しいマップを作成するたびに、IDとImageをデータファイルに保存
     *
     * @param id - MapView ID
     * @param partId - int partId
     */
    fun saveImage(id: Int, partId: Int?) {
        data!!["ids.$id"] = partId
        saveData()
    }

    // データファイルからHashMapに画像を読み込む
    private fun loadImages() {
        if (data!!.contains("ids")) Objects.requireNonNull(data!!.getConfigurationSection("ids"))?.getKeys(false)
            ?.forEach(
                Consumer { id: String ->
                    savedImages[id.toInt()] = data!!.getInt(
                        "ids.$id"
                    )
                })
    }

    fun hasImage(id: Int): Boolean {
        return savedImages.containsKey(id)
    }

    fun getImage(id: Int): Int? {
        return savedImages[id]
    }

    val data: FileConfiguration?
        get() = dataFile.config

    fun saveData() {
        dataFile.saveConfig()
    }

    internal class CustomFile(private val name: String) {
        private var plugin = Main.plugin
        private var dataConfig: FileConfiguration? = null
        private var dataConfigFile: File? = null

        init {
            saveDefaultConfig()
        }

        fun reloadConfig() {
            if (dataConfigFile == null)
                dataConfigFile = File(plugin.dataFolder, name)
            dataConfig = YamlConfiguration.loadConfiguration(dataConfigFile!!)
            val defConfigStream = plugin.getResource(name)
            if (defConfigStream != null) {
                val defConfig = YamlConfiguration.loadConfiguration(InputStreamReader(defConfigStream))
                (dataConfig as YamlConfiguration).setDefaults(defConfig)
            }
        }

        val config: FileConfiguration?
            get() {
                if (dataConfig == null) reloadConfig()
                return dataConfig
            }

        fun saveConfig() {
            if (dataConfig == null || dataConfigFile == null) return
            try {
                config!!.save(dataConfigFile!!)
            } catch (e: IOException) {
                plugin!!.logger.log(Level.SEVERE, "Could not save config to " + dataConfigFile, e)
            }
        }

        fun saveDefaultConfig() {
            if (dataConfigFile == null)
                dataConfigFile = File(plugin.dataFolder, name)
            if (dataConfigFile?.exists() != true)
                plugin.saveResource(name, true)
        }
    }

    companion object {
        var instance: MapManager? = null
            get() {
                if (field == null)
                    field = MapManager()
                return field
            }
            private set
    }
}
