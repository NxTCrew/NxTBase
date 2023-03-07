package nxt.base.extensions

import com.google.gson.Gson
import com.google.gson.JsonElement
import de.fruxz.ascend.extension.logging.getItsLogger
import de.fruxz.ascend.json.fromJsonStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import nxt.base.extensions.types.ExtensionInfo
import nxt.base.extensions.types.NxTExtension
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.nio.charset.Charset

class ExtensionsManager(private val mainPlugin: Plugin) {

    private val extensionsFolder = File(mainPlugin.dataFolder, "extensions")
    private  val gson = Gson()

    private val availableExtensions = mutableMapOf<String, ExtensionInfo>()
    private val loadedExtensions = mutableMapOf<String, NxTExtension>()

    init {
        preLoadExtensions()
        runBlocking { checkDependencies() }
        loadExtensions()
    }

    private fun preLoadExtensions() {
        extensionsFolder.mkdirs()
        extensionsFolder.listFiles()?.forEach { file ->
            if (file.extension == "jar") {
                // Load the patch.json from inside the pathFile using ZipFile and ZipEntry
                val zipFile = java.util.zip.ZipFile(file)
                val zipEntry = zipFile.getEntry("extension.json")
                val inputStream = zipFile.getInputStream(zipEntry)
                val extensionInfo = inputStream.fromJsonStream<ExtensionInfo>()

                availableExtensions[extensionInfo.name] = extensionInfo
            }
        }

        getItsLogger().info("Loaded ${availableExtensions.size} extensions.")
    }

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

    private fun loadExtensions() {
        val classLoaderParent = javaClass.classLoader
        val classLoader = java.net.URLClassLoader(extensionsFolder.listFiles()?.map { it.toURI().toURL() }?.toTypedArray(), classLoaderParent)

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