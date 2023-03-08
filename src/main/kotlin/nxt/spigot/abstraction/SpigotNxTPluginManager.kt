package nxt.spigot.abstraction

import nxt.base.abstraction.NxTPluginManager
import org.bukkit.plugin.PluginManager
import java.io.File

class SpigotNxTPluginManager(private val pluginManager: PluginManager) : NxTPluginManager() {

    override fun isPluginEnabled(pluginName: String): Boolean {
        return pluginManager.isPluginEnabled(pluginName)
    }

    override fun loadPlugin(pluginFile: File) {
        pluginManager.loadPlugin(pluginFile)
    }

}
