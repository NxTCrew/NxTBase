package nxt.base.extensions.types

import nxt.base.abstraction.NxTPlugin
import java.io.File

abstract class NxTExtension() {

    lateinit var pluginInfo: ExtensionInfo
        internal set // Only the ExtensionsManager should be able to set this

    lateinit var mainPlugin: NxTPlugin
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