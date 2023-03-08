package nxt.spigot.commands

import nxt.base.NxTBase
import nxt.base.reflection.types.NxTCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

@NxTCommand(
    namespace = "nxt",
    name = "extensions",
    description = "Manage extensions",
    usage = "/extensions",
    aliases = ["ext"],
    permission = "nxt.extensions"
)
class ExtensionsCommand : CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        when(args.getOrNull(0)) {
            "reload" -> {
                NxTBase.instance.extensionsManager.reloadExtensions()
                sender.sendMessage("§aExtensions reloaded!")
            }
            "list" -> {
                sender.sendMessage("§aExtensions:")
                NxTBase.instance.extensionsManager.loadedExtensions.forEach { (name, extension) ->
                    sender.sendMessage("§7- §a$name §7(§a${extension.pluginInfo.version}§7)")
                }
            }
            else -> return false
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        return when(args.size) {
            1 -> mutableListOf("reload", "list")
            else -> mutableListOf()
        }
    }

}