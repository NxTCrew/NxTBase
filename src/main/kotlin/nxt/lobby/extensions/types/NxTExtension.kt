package nxt.lobby.extensions.types

import org.bukkit.plugin.Plugin
import java.io.File

abstract class NxTExtension() {

    lateinit var pluginInfo: ExtensionInfo
    private lateinit var mainPlugin: Plugin

    /**
     * Creates a new extension. (Should not be called manually)
     */
    constructor(mainPlugin: Plugin, pluginInfo: ExtensionInfo) : this() {
        this.mainPlugin = mainPlugin
        this.pluginInfo = pluginInfo
    }

    /**
     * Called when the extension is enabled.
     */
    abstract fun onEnable()

    /**
     * Called when the extension is disabled.
     */
    abstract fun onDisable()

    val dataFolder
        get() = File(mainPlugin.dataFolder, "extensions/${pluginInfo.name}")


}