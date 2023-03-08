package nxt.base

import nxt.base.extensions.ExtensionsManager

class NxTBase {

    companion object {
        val instance: NxTBase = NxTBase()
    }

    lateinit var extensionsManager: ExtensionsManager

    fun onEnable() {
        // Check for extensions updates
        // Load config
        // Load commands
        // Load listeners
    }


    fun onDisable() {
        // Save config
    }
}