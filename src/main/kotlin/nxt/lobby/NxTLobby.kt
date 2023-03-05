package nxt.lobby

import nxt.lobby.extensions.ExtensionsManager
import org.bukkit.plugin.java.JavaPlugin

class NxTLobby : JavaPlugin() {

    lateinit var extensionsManager: ExtensionsManager

    // Plugin startup logic
    override fun onEnable() {
        // Check for extensions updates
        this.extensionsManager = ExtensionsManager(this)
        // Load config
        // Load commands
        // Load listeners
    }

    // Plugin shutdown logic
    override fun onDisable() {
        // Run extensions shutdown
        // Save config
    }


}