package red.man10.display.itemframe

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tororo1066.itemframeprotector.api.event.IFPRemoveEvent

class IFPListener : Listener {

    @EventHandler
    fun event(e: IFPRemoveEvent) {
        ItemFrameListener.getEntityAsMap(Bukkit.getEntity(e.data.uuid) ?: return) ?: return
        e.isCancelled = false
    }
}