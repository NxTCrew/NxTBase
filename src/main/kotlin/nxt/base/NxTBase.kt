package nxt.base

import kotlinx.coroutines.*
import nxt.base.extensions.ExtensionsManager
import nxt.base.reflection.ReflectionManager

class NxTBase {

    companion object {
        val instance: NxTBase = NxTBase()
    }

    lateinit var extensionsManager: ExtensionsManager
    lateinit var reflectionManager: ReflectionManager

    private val dispatcher = Dispatchers.Default
    internal val ioDispatcher = Dispatchers.IO
    internal val coroutineScope = CoroutineScope(dispatcher)

    fun onEnable() {
        // Check for extensions updates
        // Load config
        // Load commands
        // Load listeners
        // Load config

        // Load commands and listeners
        coroutineScope.launch {
            reflectionManager.loadBaseReflections()
        }
    }


    fun onDisable() {
        // Save config

        // Run extensions shutdown
        coroutineScope.coroutineContext.cancelChildren()
        coroutineScope.cancel()
    }
}