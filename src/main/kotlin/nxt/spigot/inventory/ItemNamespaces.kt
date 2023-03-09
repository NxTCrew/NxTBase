package nxt.spigot.inventory

import nxt.spigot.NxTSpigot
import org.bukkit.NamespacedKey

internal val NAMESPACE_PLUGIN = NxTSpigot.instance

// Gui System
val NAMESPACE_GUI_IDENTIFIER = NamespacedKey(NAMESPACE_PLUGIN, "gui_identifier")
val NAMESPACE_ITEM_IDENTIFIER = NamespacedKey(NAMESPACE_PLUGIN, "item_identifier")

// Colors
const val TEXT_GRAY = "<color:#b2c2d4>"
const val TEXT_GRADIENT_DEFAULT = "<gradient:#f6e58d:#ffbe76>"