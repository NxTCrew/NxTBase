package nxt.spigot.abstraction

import nxt.base.abstraction.NxTPlugin
import org.bukkit.plugin.Plugin

class SpigotNxTPlugin(val spigotPlugin: Plugin) : NxTPlugin() {

    override val server = SpigotNxTServer(spigotPlugin.server)

    override val dataFolder = spigotPlugin.dataFolder

}