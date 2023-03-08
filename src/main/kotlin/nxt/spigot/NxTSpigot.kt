package nxt.spigot

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nxt.base.reflection.ReflectionManager
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


    lateinit var extensionsManager: ExtensionsManager
    private lateinit var reflectionManager: ReflectionManager


    // Plugin startup logic
    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnable() {
        reflectionManager = ReflectionManager(this)
        // Check for extensions updates
        this.extensionsManager = ExtensionsManager(this, reflectionManager)
        // Load config

        // Load commands and listeners
        GlobalScope.launch {
            reflectionManager.loadBaseReflections()
        }
    }

    // Plugin shutdown logic
    override fun onDisable() {
        // Run extensions shutdown
        // Save config
    }


}