package nxt.lobby.extensions.types

data class ExtensionInfo(
    /**
     * The name of the extension.
     */
    val name: String,

    /**
     * The main class of the extension. This class must extend [NxTExtension].
     */
    val main: String,

    /**
     * The version of the extension.
     */
    val version: String = "1.0.0",

    /**
     * The authors of the extension.
     */
    val authors: Set<String> = setOf(),

    /**
     * The description of the extension.
     */
    val description: String = "",

    /**
     * The website of the extension.
     */
    val website: String = "",

    /**
     * The dependencies of the extension.
     */
    val dependencies: Set<String> = setOf(),

    /**
     * The extensions that must be loaded before this extension.
     */
    val loadBefore: Set<String> = setOf(),

    /**
     * The extensions that must be loaded after this extension.
     */
    val loadAfter: Set<String> = setOf(),

    /**
     * The external plugins that must be loaded before this extension. (will be downloaded from the spiget api, if not found)
     */
    val pluginDependencies: Set<String> = setOf(),
)
