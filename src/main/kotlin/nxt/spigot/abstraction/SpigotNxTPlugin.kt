package nxt.spigot.abstraction

import nxt.base.abstraction.NxTPlugin
import org.bukkit.plugin.Plugin

class SpigotNxTPlugin(private val plugin: Plugin) : NxTPlugin() {

    override val server = SpigotNxTServer(plugin.server)

    override val dataFolder = plugin.dataFolder

}