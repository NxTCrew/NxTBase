package nxt.lobby

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nxt.lobby.extensions.ExtensionsManager
import nxt.lobby.reflection.ReflectionManager
import org.bukkit.plugin.java.JavaPlugin

class NxTLobby : JavaPlugin() {

    companion object {
        lateinit var instance: NxTLobby
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
            reflectionManager.loadLobbyReflections()
        }
    }

    // Plugin shutdown logic
    override fun onDisable() {
        // Run extensions shutdown
        // Save config
    }


}