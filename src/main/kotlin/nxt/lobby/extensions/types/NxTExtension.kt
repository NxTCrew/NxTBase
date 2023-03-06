package nxt.lobby.extensions.types

import org.bukkit.plugin.Plugin
import java.io.File

abstract class NxTExtension() {

    lateinit var pluginInfo: ExtensionInfo
        internal set // Only the ExtensionsManager should be able to set this

    lateinit var mainPlugin: Plugin
        internal set // Only the ExtensionsManager should be able to set this

    /**
     * Called when the extension is loaded.
     */
    abstract fun onLoad()

    /**
     * Called when the extension is enabled.
     */
    abstract fun onEnable()

    /**
     * Called when the extension is unloaded.
     */
    abstract fun onDisable()


    val dataFolder: File
        get() = File(mainPlugin.dataFolder, "extensions/${pluginInfo.name}")
}