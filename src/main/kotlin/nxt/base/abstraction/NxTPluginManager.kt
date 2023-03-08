package nxt.base.abstraction

import java.io.File

abstract class NxTPluginManager {

    abstract fun isPluginEnabled(pluginName: String): Boolean

    abstract fun loadPlugin(pluginFile: File)

}