package nxt.lobby.extensions

import com.google.gson.Gson
import com.google.gson.JsonElement
import de.fruxz.ascend.extension.logging.getItsLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import nxt.lobby.extensions.types.ExtensionInfo
import nxt.lobby.extensions.types.NxTExtension
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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
            if (file.isDirectory) {
                // Load the patch.json from inside the pathFile using ZipFile and ZipEntry
                val zipFile = java.util.zip.ZipFile(file)
                val zipEntry = zipFile.getEntry("extension.json")
                val inputStream = zipFile.getInputStream(zipEntry)
                val extensionInfo = gson.fromJson(inputStream.reader(), ExtensionInfo::class.java)

                availableExtensions[extensionInfo.name] = extensionInfo
            }
        }

        getItsLogger().info("Loaded ${availableExtensions.size} extensions.")
    }

    private suspend fun checkDependencies() {
        availableExtensions.forEach { (name, extensionInfo) ->
            extensionInfo.dependencies.forEach { dependency ->
                if (!availableExtensions.containsKey(dependency)) {
                    getItsLogger().warning("Extension $name has a dependency on $dependency, but it is not loaded.")
                }
            }
            extensionInfo.pluginDependencies.forEach { dependency ->
                if (!mainPlugin.server.pluginManager.isPluginEnabled(dependency)) {
                    getItsLogger().warning("Extension $name has a dependency on $dependency, but it is not loaded.")

                    // Try to download the plugin from spiget
                    downloadPlugin(dependency) {
                        getItsLogger().info("Downloaded $dependency successfully.")
                        mainPlugin.server.pluginManager.loadPlugin(it)
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
            val fileToWrite = File(mainPlugin.dataFolder, "plugins/$pluginName.jar")

            Files.copy(url.openStream(), fileToWrite.toPath(), StandardCopyOption.REPLACE_EXISTING)
            onSuccess(fileToWrite)
        }
    }

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
            val extensionClass = Class.forName(extensionInfo.main)
            val extension = extensionClass.getConstructor(Plugin::class.java, ExtensionInfo::class.java).newInstance(mainPlugin, extensionInfo) as NxTExtension
            loadedExtensions[name] = extension
        }

        getItsLogger().info("Loaded ${loadedExtensions.size} extensions.")
    }




}