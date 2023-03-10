package nxt.base.reflection

import de.fruxz.ascend.extension.logging.getItsLogger
import kotlinx.coroutines.*
import nxt.base.NxTBase
import nxt.base.abstraction.NxTPlugin
import nxt.base.reflection.types.NxTCommand
import nxt.spigot.abstraction.SpigotNxTPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabCompleter
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import org.reflections8.Reflections
import java.lang.reflect.Field
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * The reflection manager loads all commands and listener inside the plugin
 * and it's extensions
 * @author NxTCrew
 * @since 0.0.6
 */
class ReflectionManager internal constructor(private val mainPlugin: NxTPlugin) {


    /**
     * Loads all internal (lobby) reflections (commands and listeners)
     * @author NxTCrew
     * @since 0.0.6
     * @see Reflections
     */
    internal suspend fun loadBaseReflections() {
        withContext(NxTBase.instance.ioDispatcher) {
            val baseName = "nxt.spigot"
            val reflections = Reflections(baseName)
            val timeForListeners = registerListeners(reflections, baseName)
            val timeForCommands = registerCommands(reflections, baseName)
            getItsLogger().info("Loaded internal commands and listeners in ${(timeForListeners + timeForCommands)}")
        }
    }

    /**
     * Loads all commands
     * @param reflections The reflections Object to use
     * @author NxTCrew
     * @since 0.0.6
     * @see Reflections
     */
    @OptIn(ExperimentalTime::class)
    private fun registerCommands(reflections: Reflections, baseName: String = "nxt"): Duration {
        var amountCommands = 0
        val timeForCommands = measureTime {
            val loadedCommands = mutableListOf<PluginCommand>()
            for (clazz in reflections.getTypesAnnotatedWith(NxTCommand::class.java)) {
                try {
                    val command = registerCommand(clazz)
                    if (command != null) {
                        loadedCommands.add(command)
                        amountCommands++
                    }
                } catch (exception: InstantiationError) {
                    exception.printStackTrace()
                } catch (exception: IllegalAccessException) {
                    exception.printStackTrace()
                }
            }
            registerCommands(*loadedCommands.toTypedArray())
        }
        getItsLogger().info("Loaded $amountCommands Commands from $baseName in $timeForCommands")
        return timeForCommands
    }

    fun registerCommand(clazz: Class<*>): PluginCommand? {
        val annotation = clazz.getAnnotation(NxTCommand::class.java)
        val pluginClass: Class<PluginCommand> = PluginCommand::class.java
        val constructor = pluginClass.getDeclaredConstructor(String::class.java, Plugin::class.java)

        constructor.isAccessible = true

        if (mainPlugin !is SpigotNxTPlugin) return null
        val command: PluginCommand = constructor.newInstance(annotation.name, mainPlugin.spigotPlugin)

        command.aliases = annotation.aliases.toList()
        command.description = annotation.description
        command.permission = Permission(annotation.permission, annotation.permissionDefault).name
        command.usage = annotation.usage
        command.label = annotation.namespace
        val commandInstance = clazz.getDeclaredConstructor().newInstance() as CommandExecutor
        command.setExecutor(commandInstance)
        command.tabCompleter = commandInstance as? TabCompleter
        return command
    }

    /**
     * Loads all listeners
     * @param reflections The reflections Object to use
     * @author NxTCrew
     * @since 0.0.6
     * @see Reflections
     */
    @OptIn(ExperimentalTime::class)
    private fun registerListeners(reflections: Reflections, baseName: String = "nxt"): Duration {
        var amountListeners = 0
        val timeForListeners = measureTime {
            for (clazz in reflections.getSubTypesOf(Listener::class.java)) {
                registerListener(clazz)
                amountListeners++
            }
        }
        getItsLogger().info("Loaded $amountListeners Listeners from $baseName in $timeForListeners")
        return timeForListeners
    }

    fun registerListener(clazz: Class<out Listener>): Listener? {
        try {
            val constructor = clazz.declaredConstructors.find { it.parameterCount == 0 } ?: return null

            if (clazz.`package`.name.contains("conversations")) return null

            constructor.isAccessible = true

            val event = constructor.newInstance() as Listener

            if (mainPlugin is SpigotNxTPlugin) {
                Bukkit.getPluginManager().registerEvents(event, mainPlugin.spigotPlugin)
            }

            getItsLogger().info("Listener ${event.javaClass.simpleName} registered")
            return event
        } catch (exception: InstantiationError) {
            exception.printStackTrace()
        } catch (exception: IllegalAccessException) {
            exception.printStackTrace()
        }
        return null
    }


    /**
     * Register command(s) into the server command map.
     * @param commands The command(s) to register
     * @author HexedHero
     * @see <a href="https://www.spigotmc.org/threads/cannot-register-command-with-commandmap.577072/#post-4496186">SpigotForums</a>
     * @since 0.0.6
     */
    fun registerCommands(vararg commands: PluginCommand) {
        val commandMap: CommandMap? = unsafeCommandMap()

        // Register all the commands into the map
        for (command in commands) {
            commandMap?.register(command.label, command)
            getItsLogger().info("Command `${command.name}` registered")
        }
    }

    private fun unsafeCommandMap(): CommandMap? {
        val commandMapField: Field?
        var commandMap: CommandMap? = null

        // Get the commandMap
        try {
            commandMapField = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
            commandMapField.isAccessible = true
            commandMap = commandMapField.get(Bukkit.getServer()) as CommandMap
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return commandMap
    }


    fun unregisterListeners(vararg listeners: Listener) {
        for (listener in listeners) {
            HandlerList.unregisterAll(listener)
        }
    }

    fun unregisterCommands(vararg commands: PluginCommand) {
        for (command in commands) {
            unsafeCommandMap()?.let { command.unregister(it) }
        }
    }
}