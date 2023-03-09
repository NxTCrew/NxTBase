package nxt.spigot

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import nxt.base.NxTBase
import nxt.base.extensions.ExtensionsManager
import nxt.base.reflection.ReflectionManager
import nxt.spigot.abstraction.SpigotNxTPlugin
import org.bukkit.plugin.java.JavaPlugin


class NxTSpigot : JavaPlugin() {

    companion object {
        lateinit var instance: NxTSpigot
            private set
    }

    init {
        instance = this
    }

    private var adventure: BukkitAudiences? = null
    fun adventure(): BukkitAudiences {
        checkNotNull(adventure) { "Tried to access Adventure when the plugin was disabled!" }
        return adventure as BukkitAudiences
    }

    // Plugin startup logic
    override fun onEnable() {
        // Initialize an audiences instance for the plugin
        this.adventure = BukkitAudiences.create(this)

        NxTBase.instance.reflectionManager = ReflectionManager(SpigotNxTPlugin(this))
        NxTBase.instance.extensionsManager =
            ExtensionsManager(SpigotNxTPlugin(this), NxTBase.instance.reflectionManager)
        NxTBase.instance.onEnable()

    }

    // Plugin shutdown logic
    override fun onDisable() {
        // Run extensions shutdown
        // Save config
        NxTBase.instance.onDisable()

        if(this.adventure != null) {
            this.adventure?.close()
            this.adventure = null
        }
    }

}