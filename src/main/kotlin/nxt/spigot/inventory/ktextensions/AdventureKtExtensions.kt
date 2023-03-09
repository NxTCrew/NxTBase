package nxt.spigot.inventory.ktextensions

import de.fruxz.stacked.extension.asPlainString
import net.kyori.adventure.text.Component
import org.bukkit.inventory.meta.ItemMeta

fun ItemMeta?.lore(lore: List<Component>) {
    this?.lore = lore.map { it.asPlainString }
}

fun ItemMeta?.displayName(displayName: Component) {
    this?.setDisplayName(displayName.asPlainString)
}