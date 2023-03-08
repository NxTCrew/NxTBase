package nxt.base.reflection.types

import org.bukkit.permissions.PermissionDefault

/**
 * This annotation is used to mark a class as a command.
 * The class must extend the [org.bukkit.command.CommandExecutor] class.
 * The class must have a constructor with no parameters.
 * @author NxTCrew
 * @since 0.0.6
 * @see org.bukkit.command.CommandExecutor
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class NxTCommand (
    /**
     * The name of the command. This is the name that will be used to execute the command.
     * Example: /name <args>
     */
    val name: String,

    /**
     * The namespace of the command. This is the namespace that will be used to execute the command.
     * Example: /namespace:name <args>
     */
    val namespace: String = "nxt",

    /**
     * The aliases of the command. These are the names that will be used to execute the command.
     * Example: /alias <args>
     */
    val aliases: Array<String> = [],

    /**
     * The description of the command. This is the description that will be shown in the help menu.
     */
    val description: String = "No description provided",

    /**
     * The usage of the command. This is the usage that will be shown in the help menu.
     */
    val usage: String = "Unknown usage",

    /**
     * The authors of the command. This is the authors that will be shown in the help menu.
     */
    val authors: Array<String> = ["Unknown"],

    /**
     * The permission of the command. This is the permission that will be required to execute the command.
     * Leave empty to disable permission checking.
     */
    val permission: String = "",

    /**
     * If the permission is set, this is the default execution policy value of the permission.
     * If the permission is not set, this value will be ignored.
     */
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
)
