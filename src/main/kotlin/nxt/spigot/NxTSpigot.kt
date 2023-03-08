package nxt.spigot

import nxt.base.NxTBase
import nxt.base.extensions.ExtensionsManager
import org.bukkit.plugin.java.JavaPlugin

class NxTSpigot : JavaPlugin() {

    companion object {
        lateinit var instance: NxTSpigot
            private set
    }

    init {
        instance = this
    }


    // Plugin startup logic
    override fun onEnable() {
        NxTBase.instance.extensionsManager = ExtensionsManager(this)
        NxTBase.instance.onEnable()
    }

    // Plugin shutdown logic
    override fun onDisable() {
        // Run extensions shutdown
        // Save config
        NxTBase.instance.onDisable()
    }


}