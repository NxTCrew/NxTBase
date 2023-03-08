package nxt.spigot.abstraction

import nxt.base.abstraction.NxTPlayer
import nxt.spigot.NxTSpigot
import java.util.*

class SpigotNxTPlayer(override val uuid: UUID) : NxTPlayer(uuid) {

    override var playerObject: Any = NxTSpigot.instance.server.getOfflinePlayer(uuid)

    override var name: String = "Unknown"
    override var isOnline: Boolean = false

    init {
        val offlinePlayer = playerObject as org.bukkit.OfflinePlayer
        name = offlinePlayer.name ?: "Unknown"
        isOnline = offlinePlayer.isOnline
    }

    override fun sendMessage(message: String) {
        if (isOnline) {
            (playerObject as org.bukkit.entity.Player).sendMessage(message)
        }
    }
}