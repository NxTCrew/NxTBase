package nxt.base.extensions

import com.google.gson.Gson
import com.google.gson.JsonElement
import de.fruxz.ascend.extension.logging.getItsLogger
import de.fruxz.ascend.json.fromJsonStream
import kotlinx.coroutines.*
import nxt.base.reflection.ReflectionManager
import nxt.base.reflection.types.NxTCommand
import org.bukkit.event.Listener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import nxt.base.abstraction.NxTPlugin
import nxt.base.extensions.types.ExtensionInfo
import nxt.base.extensions.types.NxTExtension
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URLClassLoader
import java.nio.charset.Charset

@OptIn(DelicateCoroutinesApi::class)
class ExtensionsManager internal constructor(private val mainPlugin: NxTPlugin, private val reflectionManager: ReflectionManager) {

    private val extensionsFolder = File(mainPlugin.dataFolder, "extensions")
    private  val gson = Gson()

    private val availableExtensions = mutableMapOf<String, ExtensionInfo>()
    val loadedExtensions = mutableMapOf<String, NxTExtension>()

    private val classLoaderParent = javaClass.classLoader
    private val classLoader = URLClassLoader(extensionsFolder.listFiles()?.map { it.toURI().toURL() }?.toTypedArray(), classLoaderParent)

    private val loadedClasses = mutableMapOf<String, Class<*>>()

    init {
        preLoadExtensions()
        runBlocking { checkDependencies() }
        loadExtensions()
        GlobalScope.launch {
            reflectionManager.loadExtensionReflections(loadedExtensions.values.toList())
        }
    }

    internal fun reloadExtensions() {
        loadedExtensions.values.forEach { it.onDisable() }
        loadedExtensions.clear()
        loadedClasses.clear()
        preLoadExtensions()
        runBlocking { checkDependencies() }
        loadExtensions()
        GlobalScope.launch {
            reflectionManager.loadExtensionReflections(loadedExtensions.values.toList())
        }
    }

    /**
     * Preloads all extensions.
     * This will load the extension.json from the extensions and add them to the [availableExtensions] map.
     * @author NxTCrew
     * @since 0.0.1
     * @see ExtensionInfo
     */
    private fun preLoadExtensions() {
        extensionsFolder.mkdirs()
        extensionsFolder.listFiles()?.forEach { file ->
            if (file.extension == "jar") {
                // Load the patch.json from inside the pathFile using ZipFile and ZipEntry
                val zipFile = java.util.zip.ZipFile(file)
                val zipEntry = zipFile.getEntry("extension.json")
                val inputStream = zipFile.getInputStream(zipEntry)
                val extensionInfo = inputStream.fromJsonStream<ExtensionInfo>()

                // Load all .class files from the jar file and add them to the class loader
                /*val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.endsWith(".class")) {
                        try {
                            val clazz = Class.forName(entry.name.replace("/", ".").substringBeforeLast("."), false, classLoader)
                            if (clazz.annotations.any { it is NxTCommand }) {
                                reflectionManager.registerCommand(clazz)
                            }
                            try {
                                val listener = clazz.asSubclass(Listener::class.java)
                                reflectionManager.registerListener(listener)
                            } catch (e: ClassCastException) {
                                // Ignore
                            }
                            loadedClasses["${clazz.packageName}.${clazz.name}"] = clazz
                            println("Loaded class ${clazz.packageName}.${clazz.name}")
                        } catch (e: ClassNotFoundException) {
                            getItsLogger().warning("Could not load class ${entry.name.replace("/", ".").substringBeforeLast(".")}")
                        }
                    }
                }*/
                val jarEntries = zipFile.entries()
                jarEntries?.toList()?.forEach { entry ->
                    if (!entry.name.endsWith(".class")) return@forEach
                    val className = entry.name.replace('/', '.').dropLast(6)
                    val clazz = classLoader.loadClass(className)
                    if (clazz.annotations.any { it is NxTCommand }) {
                        reflectionManager.registerCommand(clazz)
                    }
                    try {
                        val listener = clazz.asSubclass(Listener::class.java)
                        reflectionManager.registerListener(listener)
                    } catch (e: ClassCastException) {
                        // Ignore
                    }
                    loadedClasses["${clazz.packageName}.${clazz.name}"] = clazz
                    println("Loaded class ${clazz.packageName}.${clazz.name}")
                }

                availableExtensions[extensionInfo.name] = extensionInfo
            }
        }

        getItsLogger().info("Loaded ${availableExtensions.size} extensions.")
    }

    /**
     * Checks if the extensions have all dependencies loaded.
     * If not, it will try to download the plugin from spiget.
     * @author NxTCrew
     * @since 0.0.1
     */
    private suspend fun checkDependencies() {
        availableExtensions.forEach { (name, extensionInfo) ->
            if(extensionInfo.dependencies.isEmpty() && extensionInfo.pluginDependencies.isEmpty()) return@forEach

            if(extensionInfo.dependencies.isNotEmpty()) {
                extensionInfo.dependencies.forEach { dependency ->
                    if (!availableExtensions.containsKey(dependency)) {
                        getItsLogger().warning("Extension $name has a dependency on $dependency, but it is not loaded.")
                    }
                }
            }

            if(extensionInfo.pluginDependencies.isEmpty()) return@forEach
            extensionInfo.pluginDependencies.forEach { dependency ->
                if (!mainPlugin.server.pluginManager.isPluginEnabled(dependency)) {
                    getItsLogger().warning("Extension $name has a dependency on $dependency, but it is not loaded.")

                    // Try to download the plugin from spiget
                    try {
                        downloadPlugin(dependency) {
                            getItsLogger().info("Downloaded $dependency successfully.")
                            mainPlugin.server.pluginManager.loadPlugin(it)
                        }
                    } catch (e: Exception) {
                        getItsLogger().warning("Could not load $dependency.")
                        getItsLogger().warning(e.message)
                    }
                }
            }
        }

        getItsLogger().info("Checked dependencies.")
    }

    /**
     * Downloads a plugin from spiget.
     * @param pluginName The name of the plugin.
     * @param onSuccess The function that should be called when the plugin is downloaded.
     * @throws Exception If the plugin could not be downloaded.
     * @author NxTCrew
     * @since 0.0.4
     */
    private suspend fun downloadPlugin(pluginName: String, onSuccess : suspend (plugin: File) -> Unit = {}) {
        withContext(Dispatchers.IO) {
            val infoUrl = java.net.URL("https://api.spiget.org/v2/search/resources/$pluginName?sort=-downloads")
            val pluginInfo = infoUrl.readText(Charset.defaultCharset())
            val pluginId = gson.fromJson(pluginInfo, JsonElement::class.java).asJsonArray[0].asJsonObject["id"].asInt


            val url = java.net.URL("https://api.spiget.org/v2/resources/$pluginId/download")
            val fileToWrite = File(mainPlugin.dataFolder, "../$pluginName.jar")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "NxTAgent/1.0")

            val inputStream = connection.inputStream
            val fileOutputStream = FileOutputStream(fileToWrite)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                fileOutputStream.write(buffer, 0, bytesRead)
            }
            fileOutputStream.close()
            inputStream.close()
            onSuccess(fileToWrite)
        }
    }

    /**
     * Loads all extensions.
     * This will load the extensions in the correct order.
     * @author NxTCrew
     * @since 0.0.1
     * @see ExtensionInfo
     * @see NxTExtension
     */
    private fun loadExtensions() {
        availableExtensions.toSortedMap { ex1, ex2 ->
            val ex1Info = availableExtensions[ex1]!!
            val ex2Info = availableExtensions[ex2]!!
            var loadOrder = 0

            loadOrder = if (ex1Info.loadAfter.contains(ex2)) {
                1
            } else if (ex2Info.loadAfter.contains(ex1)) {
                -1
            } else {
                0
            }

            if(ex1Info.loadBefore.contains(ex2)) {
                -1
            } else if (ex2Info.loadBefore.contains(ex1)) {
                1
            } else {
                loadOrder
            }
        }.forEach { (name, extensionInfo) ->
            try {
                val extensionClass = classLoader.loadClass(extensionInfo.main)
                val constructor = extensionClass.getConstructor()
                constructor.isAccessible = true
                val extension = constructor.newInstance() as NxTExtension
                extension.mainPlugin = mainPlugin
                extension.pluginInfo = extensionInfo

                loadedExtensions[name] = extension
                extension.onLoad()
            } catch (e: Exception) {
                getItsLogger().warning("Could not load extension $name.")
                getItsLogger().warning(e.stackTraceToString())
            }
        }

        getItsLogger().info("Loaded ${loadedExtensions.size} extensions.")
    }




}