package nxt.spigot.abstraction

import nxt.base.abstraction.NxTServer
import org.bukkit.Server

class SpigotNxTServer(val server: Server) : NxTServer() {


    override val pluginManager = SpigotNxTPluginManager(server.pluginManager)

}
